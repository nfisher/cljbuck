package ca.junctionbox.cljbuck.lexer.benchmarks;

import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.Queue;

public class LexerBenchmark {
    /*
    @Benchmark
    public void BenchmarkLexer() {
        final One2OneChannel<Object> chan = Channel.one2one(new Buffer(2048));
        final Lexable l = Lexable.create("comment.clj",
                "(ns my.core (:require [hello.world.extended :as ext])\n\n\n(defn hello [name]\n (prn \"Hola \" name))", chan.out());

        ConsumeTask task = new ConsumeTask(chan.in());

        new Parallel(new CSProcess[]{
                (CSProcess) l,
                task,
        }).run();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LexerBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    */
}
