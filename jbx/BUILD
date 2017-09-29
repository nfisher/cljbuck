(clj-lib :name "lib"
         :ns "jbx.core"
         :srcs ["src/clj/**/*.clj", "src/cljc/**/*.cljc"]
         :deps ["//lib:clojure1.9"])

(clj-binary :name "main"
            :main "jbx.core"
            :deps [":lib"])

(clj-test :name "test"
          :srcs ["test/clj/**/*.clj"]
          :deps ["//lib:clojure1.9", ":lib"])
