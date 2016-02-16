package com.movies;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ivan on 2/1/16.
 *
 */
public class GenreVoteCalculater extends Thread {

    private static final String tmdbDiscoverUrl = "http://api.themoviedb.org/3/discover/movie?api_key=72b56103e43843412a992a8d64bf96e9&with_genres=%d&page=%d";

    private GenreVoteCalcResult dest;
    private int genreId;

    public GenreVoteCalculater(GenreVoteCalcResult dest, int genreId) {
        this.dest = dest;
        this.genreId = genreId;
    }

    @Override
    public void run() {
        System.out.println("begin new calc for genre: " + genreId);

        int nextPage = 1;

        while (true) {
            JSONObject moviesPage = tmdbPage(nextPage);

            if (moviesPage == null) {
                break;
            }

            try {
                if (moviesPage.getInt("total_results") == 0) {
                    dest.setError("unknown genre");
                    break;
                }

                for (Object movie : moviesPage.getJSONArray("results")) {
                    dest.addVotingSum(((JSONObject) movie).getDouble("vote_average"));
                }

                int nPages = moviesPage.getInt("total_pages");

                dest.updatePercent(nextPage, nPages); //may be its better, update it by every movie, not page as now

                if (nextPage == nPages) {
                    break;
                }
            } catch (Exception e) {
                dest.setError(e.getMessage());
                break;
            }

            nextPage++;
        }

        dest.endCalc();

        try {
            sleep(60 * 1000); // sleep for 1 minute for ensuring that client gets calculated result
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dest.reset();
    }

    JSONObject tmdbPage(int page) {
        String queryUrl = String.format(tmdbDiscoverUrl, genreId, page);
        JSONObject obj = null;
        try {
            TmdbResponse tmdbResponse = UrlRequest.executeGet(queryUrl);

            while (tmdbResponse.needWait()) { // its limit on requests at time, so lets wait 1 sec
                System.out.println("sleep 1 sec");
                sleep(1000);
                tmdbResponse = UrlRequest.executeGet(queryUrl);
            }

            obj = new JSONObject(tmdbResponse.getBody());
        } catch (IOException e) {
            dest.setError(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            return null;
        }

        return obj;
    }
}
