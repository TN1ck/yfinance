YFINANCE
========

A simple utility to download Historical stock prices from Y! Finance.

DEPENDENCIES
------------

yfinance depends on `clj-time` and `clojure.contrib.http.agent`.

USAGE
-----

    (:require [in.freegeek.yfinance :as yf])
    (yf/fetch-historical-data "2009-01-01" "2009-01-31" ["AAPL" "IBM" "MSFT" "GOOG"])

Example Output:

    {"AAPL" [{
             :adjusted_close 77.18
             :volume 11734800
             :close 84.12
             :low 82.26
             :high 84.83
             :open 83.61
             :trading_date "2009-01-15"},
             ...
            ]
     "IBM"  [...]
     "MSFT" [...]
     "GOOG" [...]}

FEEDBACK
--------

Email me: b.ghose at GMail
