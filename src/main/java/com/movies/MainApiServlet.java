package com.movies;

import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivan on 1/31/16.
 *
 */
public class MainApiServlet extends HttpServlet {

    private static String tmdbDiscoverUrl = "http://api.themoviedb.org/3/discover/movie?api_key=72b56103e43843412a992a8d64bf96e9&";
    private static String tmdbMovieUrl = "http://api.themoviedb.org/3/movie/%d?api_key=72b56103e43843412a992a8d64bf96e9";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /**
        получение списка фильмов (с возможностью фильтрования, сортировки и ограничения числа результатов)
        получение информации по конкретному фильму
        получение средней оценки по жанру. Такой запрос должен выполняться в фоне, то есть в ответ на такой запрос клиент сразу получает ответ о том, что подсчёт начался.
         Затем клиент может узнавать о ходе подсчёта, в ответ ему должен возвращаться процент обработанной информации,
                либо результат подсчёта, то есть средняя оценка. В случае нескольких параллельных запросов на подсчёт средней оценки для одного жанра должен выполняться только один подсчёт
         */

        if (request.getRequestURI().startsWith("/movies/discover")) {
            String queryUrl = tmdbDiscoverUrl;
            if (request.getQueryString() != null) {
                queryUrl = tmdbDiscoverUrl + request.getQueryString();
            }
            proxyRequest(queryUrl, response);
        } else if (request.getRequestURI().startsWith("/movies/movie")) {
            Pattern pMovieId = Pattern.compile("^/movies/movie/([0-9]+).*");
            Matcher m = pMovieId.matcher(request.getRequestURI());

            if (m.find()) {
                int movieId = Integer.parseInt(m.group(1));
                proxyRequest(String.format(tmdbMovieUrl, movieId), response);
            }
        } else if (request.getRequestURI().startsWith("/movies/genrevote")) {
            Pattern pMovieId = Pattern.compile("^/movies/genrevote/([0-9]+).*");
            Matcher m = pMovieId.matcher(request.getRequestURI());

            if (m.find()) {
                Integer genreId = Integer.parseInt(m.group(1));

                ServletContext servletContext = getServletContext();

                ConcurrentMap<Integer, GenreVoteCalcResult> mapGenreToAverageVotingObj = (ConcurrentMap<Integer, GenreVoteCalcResult>) servletContext.getAttribute("mapGenreToAverageVotingObj");
                GenreVoteCalcResult calcObj = mapGenreToAverageVotingObj.computeIfAbsent(genreId, averageVotingObj -> new GenreVoteCalcResult());
                if (calcObj.isNotStarted()) {
                    System.out.println("Start calc genre: " + genreId);
                    ExecutorService executor = (ExecutorService) servletContext.getAttribute("executor");
                    synchronized (calcObj) {
                        executor.submit(new GenreVoteCalculater(calcObj, genreId));
                        calcObj.start();
                    }

                    writeResponse("{\"percent_calc_average_vote\":0, \"genre_average_vote\":0.00}", response);
                } else if (calcObj.isEnded()) {
                    System.out.println("isEnded");
                    writeResponse(String.format("{%s\"genre_average_vote\":%.2f}", calcObj.getErrorJson(), calcObj.getResultVoting()), response);
                } else {
                    System.out.println("in process");
                    writeResponse(String.format("{%s\"percent_calc_average_vote\":%d, \"genre_average_vote\":%.2f}",
                            calcObj.getErrorJson(), calcObj.getPercent(), calcObj.getResultVoting()), response);
                }
            }
        }
    }

    private void proxyRequest(String queryUrl, HttpServletResponse response) throws IOException {
        TmdbResponse tmdbResponse = UrlRequest.executeGet(queryUrl);

        if (tmdbResponse.getBody() != null) {
            writeResponse(tmdbResponse.getBody(), response);
        } else {
            JSONObject jsonError = new JSONObject();
            jsonError.put("error", "invalid request to TMDb");
            jsonError.put("responseCodeFromTmdb", tmdbResponse.getResponseCode());

            writeResponse(jsonError.toString(), response);
        }
    }

    private void writeResponse(String s, HttpServletResponse response) throws IOException {
        OutputStream os = response.getOutputStream();

        os.write(s.getBytes());
        os.flush();
    }
}
