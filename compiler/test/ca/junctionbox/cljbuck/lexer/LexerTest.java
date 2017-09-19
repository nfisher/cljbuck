package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static org.junit.Assert.*;

public class LexerTest {

    public static final String bigFile = "; Copyright 2013 Relevance, Inc.\n" +
            "; Copyright 2014-2016 Cognitect, Inc.\n" +
            "\n" +
            "; The use and distribution terms for this software are covered by the\n" +
            "; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)\n" +
            "; which can be found in the file epl-v10.html at the root of this distribution.\n" +
            ";\n" +
            "; By using this software in any fashion, you are agreeing to be bound by\n" +
            "; the terms of this license.\n" +
            ";\n" +
            "; You must not remove this notice, or any other, from this software.\n" +
            "\n" +
            "(ns io.pedestal.interceptor.helpers\n" +
            "  (:require [io.pedestal.interceptor :as interceptor :refer [interceptor\n" +
            "                                                             interceptor-name]]))\n" +
            "\n" +
            "(defmacro definterceptor\n" +
            "  \"Define an instance of an interceptor and store it in a var. An\n" +
            "  optional doc string can be provided.\n" +
            "  The body can be anything that satisfies the IntoInterceptor protocol.\n" +
            "\n" +
            "  usage:\n" +
            "    (definterceptor encode-response\n" +
            "       \\\"An interceptor that encodes the response as json\\\"\n" +
            "       (on-response encode-json))\n" +
            "\n" +
            "  Alternatively, you may also:\n" +
            "    (def encode-response\n" +
            "      \\\"An interceptor that encodes the response as json\\\"\n" +
            "      (on-response encode-json)\"\n" +
            "  [name & body]\n" +
            "  (let [init (if (string? (first body))\n" +
            "               (second body)\n" +
            "               (first body))\n" +
            "        doc (when (string? (first body))\n" +
            "              (first body))]\n" +
            "    `(def ~(with-meta name {:doc doc})  ~(interceptor init))))\n" +
            "\n" +
            "(defn- infer-basic-interceptor-function\n" +
            "  \"Given list `args`, infer a form that will evaluate to a function\n" +
            "  from the list. The passed list will be interpreted as entirely\n" +
            "  containing information for specifying one function, and no\n" +
            "  additional information.\"\n" +
            "  [args]\n" +
            "  (cond\n" +
            "   (symbol? (first args)) (first args)\n" +
            "   (and (vector? (first args))\n" +
            "        (< 1 (count args))) `(fn ~@args)))\n" +
            "\n" +
            "(defn- infer-first-interceptor-function\n" +
            "  \"Given list `args`, infer a form that will evaluate to a function\n" +
            "  from the list. The passed list will be interpreted as potentially\n" +
            "  containing information for specifying multiple functions, and only\n" +
            "  information for the first function will be inferred from the list.\"\n" +
            "  [args]\n" +
            "  (cond\n" +
            "   (symbol? (first args)) (first args)\n" +
            "   (list? (first args)) (conj (first args) 'fn)))\n" +
            "\n" +
            "(defn infer-rest-interceptor-function\n" +
            "  \"Given list `args`, return the rest of args that would remain after\n" +
            "  removing the elements of args that specify the form returned by\n" +
            "  infer-first-interceptor-function.\"\n" +
            "  [args]\n" +
            "  ;; Right now there's no ambiguity, so just use rest.\n" +
            "  (rest args))\n" +
            "\n" +
            "(defmacro ^{:private true} defsimpleinterceptordef\n" +
            "  \"Defines a macro which is used for defining interceptor defs. `n`\n" +
            "  is the name of an interceptorfn which adapts a fn to the interceptor\n" +
            "  framework, e.g. before, `docstring` is the docstring for the macro in question.\"\n" +
            "  [n docstring]\n" +
            "  `(defmacro ~(symbol (str \"def\" (name n)))\n" +
            "     ~docstring\n" +
            "     [macro-name# & args#]\n" +
            "     (let [[docstring# args#] (if (string? (first args#))\n" +
            "                                [(first args#) (rest args#)]\n" +
            "                                [nil args#])\n" +
            "           prefix-list# (if docstring#\n" +
            "                          (list macro-name# docstring#)\n" +
            "                          (list macro-name#))\n" +
            "           fn-form# (infer-basic-interceptor-function args#)\n" +
            "           interceptor-name# (keyword (name (ns-name *ns*)) (name macro-name#))]\n" +
            "       `(definterceptor ~@prefix-list#\n" +
            "          (~~n ~interceptor-name# ~fn-form#)))))\n" +
            "\n" +
            "(defn before\n" +
            "  \"Returns an interceptor which calls `f` on context during the enter\n" +
            "  stage.\"\n" +
            "  ([f] (interceptor {:enter f}))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :enter #(apply f % args)}))))\n" +
            "\n" +
            "(defsimpleinterceptordef before \"Defines a before interceptor. The\n" +
            "  defined function performs processing during interceptor execution\n" +
            "  during the enter stage. The implicitly created function will operate\n" +
            "  on context, and return a value used as the new context, e.g.:\n" +
            "\n" +
            "  (defbefore flag-zotted\n" +
            "    [context]\n" +
            "    (assoc context :zotted true))\")\n" +
            "\n" +
            "(defn after\n" +
            "  \"Return an interceptor which calls `f` on context during the leave\n" +
            "  stage.\"\n" +
            "  ([f] (interceptor {:leave f}))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :leave #(apply f % args)}))))\n" +
            "\n" +
            "(defsimpleinterceptordef after\n" +
            "  \"Defines an after interceptor. The defined function is processed\n" +
            "  during the leave stage of interceptor execution. The implicitly\n" +
            "  created function will operate on context, and return a value used as\n" +
            "  the new context, e.g.:\n" +
            "\n" +
            "  (defafter check-zotted\n" +
            "    [context]\n" +
            "    (if-not (:zotted context)\n" +
            "      (throw (ex-info \\\"Context was not zotted!\\\"\n" +
            "                      {:context context}))\n" +
            "      context))\")\n" +
            "\n" +
            "(defn around\n" +
            "  \"Return an interceptor which calls `f1` on context during the enter\n" +
            "  stage, and calls `f2` on context during the leave stage.\"\n" +
            "  ([f1 f2]\n" +
            "     (interceptor {:enter f1\n" +
            "                   :leave f2}))\n" +
            "  ([n f1 f2]\n" +
            "     (interceptor {:name (interceptor-name n)\n" +
            "                   :enter f1\n" +
            "                   :leave f2})))\n" +
            "\n" +
            "(defmacro defaround\n" +
            "  \"Defines an around interceptor. The definition resembles a multiple\n" +
            "  arity function definition, however both fns are 1-arity. The first\n" +
            "  fn will be called during the enter stage, the second during the\n" +
            "  leave stage, e.g.:\n" +
            "\n" +
            "  (defaround aroundinterceptor\n" +
            "    ([context] (assoc context :around :entering))\n" +
            "    ([context] (assoc context :around :leaving)))\"\n" +
            "\n" +
            "  [n & args]\n" +
            "  (let [[docstring args] (if (string? (first args))\n" +
            "                           [(first args) (rest args)]\n" +
            "                           [nil args])\n" +
            "        prefix-list (if docstring\n" +
            "                      (list n docstring)\n" +
            "                      (list n))\n" +
            "        [enter-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        [leave-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        interceptor-name# (keyword (name (ns-name *ns*)) (name n))]\n" +
            "    `(definterceptor ~@prefix-list\n" +
            "       (around ~interceptor-name# ~enter-fn-form ~leave-fn-form))))\n" +
            "\n" +
            "(defn on-request\n" +
            "  \"Returns an interceptor which updates the :request value of context\n" +
            "  with f during the enter stage.\"\n" +
            "  ([f] (before (fn [context]\n" +
            "                 (assoc context :request (f (:request context))))))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :enter (fn [context]\n" +
            "                              (assoc context :request (apply f (:request context) args)))}))))\n" +
            "\n" +
            "(defsimpleinterceptordef on-request\n" +
            "  \"Defines an on-request interceptor. The definition performs\n" +
            "  pre-processing on a request during the enter stage of interceptor\n" +
            "  execution. The implicitly created interceptor will extract the\n" +
            "  request from the context it receives, pass it to the defined\n" +
            "  function, and then associate the return value from the defined\n" +
            "  function as into context with the :request key and return\n" +
            "  context, e.g.:\n" +
            "\n" +
            "  (defon-request parse-body-as-wibblefish\n" +
            "    [request]\n" +
            "    (assoc request :wibblefish-params\n" +
            "           (wibblefish-parse (:body request))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defbefore parse-body-as-wibblefish\n" +
            "    [context]\n" +
            "    (let [request (:request context)\n" +
            "          new-request (assoc request :wibblefish-params\n" +
            "                             (wibblefish-parse (:body request)))]\n" +
            "      (assoc context :request new-request)))\")\n" +
            "\n" +
            "(defn on-response\n" +
            "  \"Returns an interceptor which updates the :response value of context\n" +
            "  with f during the leave stage.\"\n" +
            "  ([f] (after (fn [context]\n" +
            "                (assoc context :response (f (:response context))))))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :leave (fn [context]\n" +
            "                              (assoc context :response (apply f (:response context) args)))}))))\n" +
            "\n" +
            "(defsimpleinterceptordef on-response\n" +
            "  \"Defines an on-response interceptor. The definition performs post\n" +
            "  processing on a response during the leave stage of interceptor\n" +
            "  execution. The implicitly created interceptor will extract the\n" +
            "  response from the context it receives, pass it to the defined\n" +
            "  function, and then associate the return value from the defined function\n" +
            "  into context with the :response key and return context, e.g.:\n" +
            "\n" +
            "  (defon-response change-body-to-html\n" +
            "    [response]\n" +
            "    (assoc response :body\n" +
            "           (render-to-html (:body response))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defafter change-body-to-html\n" +
            "    [context]\n" +
            "    (let [response (:response context)\n" +
            "          new-response (assoc response :body\n" +
            "                              (render-to-html (:body response)))]\n" +
            "      (assoc context :response new-response)))\")\n" +
            "\n" +
            "(defn handler\n" +
            "  \"Returns an interceptor which calls f on the :request value of\n" +
            "  context, and assoc's the return value as :response into context during the\n" +
            "  enter stage.\"\n" +
            "  ([f]\n" +
            "     (before (fn [context]\n" +
            "               (assoc context :response (f (:request context))))))\n" +
            "  ([n f]\n" +
            "     (before (interceptor-name n)\n" +
            "             (fn [context]\n" +
            "               (assoc context :response (f (:request context)))))))\n" +
            "\n" +
            "(defsimpleinterceptordef handler\n" +
            "  \"Defines a handler interceptor. The definition mirrors a ring-style\n" +
            "  request handler and is made in terms of a ring style request. The\n" +
            "  implicitly created interceptor will extract the request from the\n" +
            "  context it receives, pass it to the defined function, and then\n" +
            "  associate the return value from the defined function as into\n" +
            "  context with the :response key and return context, e.g.:\n" +
            "\n" +
            "  (defhandler hello-name\n" +
            "    [request]\n" +
            "    (ring.util.response/response\n" +
            "      (str \\\"Hello, \\\" (-> request\n" +
            "                           :params\n" +
            "                           :name))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defbefore hello-name\n" +
            "    [context]\n" +
            "    (let [request (:request context)\n" +
            "          response (ring.util.response/response\n" +
            "                     (str \\\"Hello, \\\" (-> request\n" +
            "                                          :params\n" +
            "                                          :name)))]\n" +
            "      (assoc context :response response)))\")\n" +
            "\n" +
            "(defn middleware\n" +
            "  \"Returns an interceptor which calls `f1` on the :request value of\n" +
            "  context during the enter stage, and `f2` on the :response value of\n" +
            "  context during the leave stage.\"\n" +
            "  ([f1 f2]\n" +
            "     (interceptor {:enter (when f1 #(update-in % [:request] f1))\n" +
            "                   :leave (when f2 #(update-in % [:response] f2))}))\n" +
            "  ([n f1 f2]\n" +
            "     (interceptor {:name (interceptor-name n)\n" +
            "                   :enter (when f1 #(update-in % [:request] f1))\n" +
            "                   :leave (when f2 #(update-in % [:response] f2))})))\n" +
            "\n" +
            "(defmacro defmiddleware\n" +
            "  \"Defines a middleware interceptor. The definition resembles a\n" +
            "  multiple arity function definition, however both fns are\n" +
            "  1-arity. The first fn will be called during the enter stage with the\n" +
            "  value of the :request key in the context, the second during the\n" +
            "  leave stage with the response key in the context, e.g.:\n" +
            "\n" +
            "  (defmiddleware middleware-interceptor\n" +
            "    ([request] (assoc request :middleware :on-request))\n" +
            "    ([response] (assoc response :middleware :on-response)))\"\n" +
            "\n" +
            "  [n & args]\n" +
            "  (let [[docstring args] (if (string? (first args))\n" +
            "                           [(first args) (rest args)]\n" +
            "                           [nil args])\n" +
            "        prefix-list (if docstring\n" +
            "                      (list n docstring)\n" +
            "                      (list n))\n" +
            "        [enter-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        [leave-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        interceptor-name# (keyword (name (ns-name *ns*)) (name n))]\n" +
            "    `(definterceptor ~@prefix-list\n" +
            "       (middleware ~interceptor-name# ~enter-fn-form ~leave-fn-form))))\n" +
            "\n";

    @Test
    public void Test_next() {
        final Lexable l = Lexable.create("test.clj", "a", null);

        char c1 = l.next();
        char c2 = l.next();

        assertEquals('a', c1);
        assertEquals(3, c2);
    }

    @Test
    public void Test_ignore() {
        final Lexable l = Lexable.create("test.clj", "   ", null);
        l.acceptRun(" ");
        l.ignore();

        assertEquals(2, l.getPos());
    }

    @Test
    public void Test_accept() {
        final Lexable l = Lexable.create("test.clj", " ) ", null);
        l.accept(" ");
        l.accept(")");
        l.accept(" ");
        assertEquals(3, l.getPos());
    }
}