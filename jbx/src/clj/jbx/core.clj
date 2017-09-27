(ns jbx.core
  (:require
    [jbx.pants :as pants])
  (:gen-class))

(defn -main []
  (pants/hello "World"))
