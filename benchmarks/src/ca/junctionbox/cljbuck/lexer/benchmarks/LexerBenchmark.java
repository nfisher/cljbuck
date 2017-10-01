package ca.junctionbox.cljbuck.lexer.benchmarks;

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
