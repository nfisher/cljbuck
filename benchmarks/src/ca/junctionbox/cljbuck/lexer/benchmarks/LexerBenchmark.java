package ca.junctionbox.cljbuck.lexer.benchmarks;

import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StringLexer;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Parallel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.Queue;

public class LexerBenchmark {

    @Benchmark
    public void BenchmarkLexer() {
        final Lexable l = new StringLexer("comment.clj",
                "(ns my.core (:require [hello.world.extended :as ext])\n\n\n(defn hello [name]\n (prn \"Hola \" name))");

        ConsumeTask task = new ConsumeTask(l);

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
}

class ConsumeTask implements CSProcess {
    final Queue<Item> items = new LinkedList<>();
    final Lexable l;

    ConsumeTask(Lexable l) {
        this.l = l;
    }

    @Override
    public void run() {
        for (;;) {
            final Item item = l.nextItem();
            if (item == null) {
                break;
            }
            items.add(item);
        }
    }
}