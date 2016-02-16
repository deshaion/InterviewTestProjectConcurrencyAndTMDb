package com.movies;

/**
 * Created by ivan on 2/1/16.
 *
 */
public class GenreVoteCalcResult {
    /**
     * so because only one thread set values, but several threads can get values
     * i'm not use synchronized methods and use volatile for all fields
     */

    private volatile double votingSum;
    private volatile int votingCount;

    private volatile int percent = 0;

    private volatile String error = null;

    private volatile boolean started = false;

    public double getResultVoting() {
        if (votingCount == 0) {
            return 0;
        }

        return votingSum / votingCount;
    }

    public void addVotingSum(double add) {
        this.votingSum += add;
        this.votingCount++;
    }

    public int getPercent() {
        return percent;
    }

    public void endCalc() {
        this.percent = 100;
    }

    public boolean isEnded() {
        return percent == 100;
    }

    public void updatePercent(int iPage, int nPages) {
        percent = 100 * iPage / nPages;
    }

    public String getErrorJson() {
        if (error == null) {
            return "";
        } else {
            return "\"error\":\"" + error + "\", ";
        }
    }

    public void setError(String error) {
        this.error = error;
    }

    public void reset() {
        started = false;
        votingCount = 0;
        votingSum = 0;
        percent = 0;
        error = null;
    }

    public boolean isNotStarted() {
        return !started;
    }

    public void start() {
        started = true;
    }
}
