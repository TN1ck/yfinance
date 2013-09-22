(ns #^{:author "Baishampayan Ghose"
       :doc "A few simple utils to download Y! Finance data"}
  in.freegeek.yfinance
  (:require [clj-time.core :as time]
            [clj-http.client :as client]
            [clojure.data.csv :as csv])
  (:import java.net.URLEncoder))

(def #^{:private true} +base-url+ "http://itable.finance.yahoo.com/table.csv?s=%s&g=d&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d&g=%s&ignore=.csv")


(defn- get-full-url
  "Construct the complete URL given the params"
  [y1 m1 d1 y2 m2 d2 p sym]
  (let [start (time/date-time y1 m1 d1)
        end (time/date-time y2 m2 d2)
        period (p {:daily "d"
                   :weekly "w"
                   :monthly "m"})]
    (format +base-url+
            (URLEncoder/encode sym "UTF-8")
            (dec (time/month start))
            (time/day start)
            (time/year start)
            (dec (time/month end))
            (time/day end)
            (time/year end)
            period)))

(defn- fetch-url
  "Fetch one URL using HTTP Agent"
  [url]
  (client/get url {:throw-exceptions false}))

(defn- collect-response
  "Wait for all the agents to finish and then return the response"
  [& responses]
  (for [response responses]
    (let [{:keys [status body]} response]
      (if (= status 200)
        body
        response))))

(defn- parse-day [header [x & xs]]
  "takes a line from Yahoo! Finance CSV data and returns
  a map where the keys are the column names"
  (let [println header
        names-adjusted
        {"Date" :trading_date
         "Open" :open
         "High" :high
         "Low" :low
         "Close" :close
         "Volume" :volume
         "Adj Close" :adjusted_close}
        header-adjusted (map names-adjusted header)]
    (zipmap header-adjusted
            (cons x (map read-string xs)))))

(defn- parse-response [resp]
  "takes the response from a yfinance query and parses it to a list"
  (if (= (:status resp) 404)
    404
    (let [[header & records] (csv/read-csv resp)]
      (map (partial parse-day header) records))))


(defn fetch-historical-data
  "Fetch historical prices from Yahoo! finance for the given symbols between start and end
   example-usage: (fetch-historical-data \"2009-01-01\" \"2009-01-15\" [:AAPL :IBM])"
  ([start end syms] (fetch-historical-data start end syms :daily))
  ([start end syms period]
     (letfn [(parse-date [^String dt] (map #(Integer/parseInt %) (.split dt "-")))]
       (let [[y1 m1 d1] (parse-date start)
             [y2 m2 d2] (parse-date end)
             urls (map (partial get-full-url y1 m1 d1 y2 m2 d2 period) (map name syms))
             ;; worth multi-threading since network should be bottle-neck
             responses (pmap fetch-url urls)]
         (zipmap syms (map parse-response (apply collect-response responses)))))))
