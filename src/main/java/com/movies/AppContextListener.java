package com.movies;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.*;

/**
 * Created by ivan on 2/1/16.
 *
 */

@WebListener
public class AppContextListener implements ServletContextListener {
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        ThreadFactory daemonFactory = new DaemonThreadFactory();

        executor = Executors.newCachedThreadPool(daemonFactory);

        ctx.setAttribute("executor", executor);

        ConcurrentMap<Integer, GenreVoteCalcResult> mapGenreToAverageVotingObj = new ConcurrentHashMap<>();

        ctx.setAttribute("mapGenreToAverageVotingObj", mapGenreToAverageVotingObj);

        System.out.println("ServletContext initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdownNow();

        System.out.println("ServletContext Destroyed.");
    }
}

class DaemonThreadFactory implements ThreadFactory {

    private final ThreadFactory factory;

    /**
     * Construct a ThreadFactory with setDeamon(true) using
     * Executors.defaultThreadFactory()
     */
    public DaemonThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    /**
     * Construct a ThreadFactory with setDeamon(true) wrapping the given factory
     *
     * @param factory
     *            factory to wrap
     */
    public DaemonThreadFactory(ThreadFactory factory) {
        if (factory == null)
            throw new NullPointerException("factory cannot be null");
        this.factory = factory;
    }

    public Thread newThread(Runnable r) {
        final Thread t = factory.newThread(r);

        t.setDaemon(true);

        return t;
    }
}
