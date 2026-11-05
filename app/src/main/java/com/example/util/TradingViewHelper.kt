package com.example.util

data class StockTickerItem(
    val code: String,
    val name: String,
    val sector: String,
    val fullSymbol: String = "IDX:$code"
)

object TradingViewHelper {

    val popularStocks = listOf(
        StockTickerItem("BBRI", "Bank Rakyat Indonesia (Persero) Tbk", "Perbankan"),
        StockTickerItem("BBCA", "Bank Central Asia Tbk", "Perbankan"),
        StockTickerItem("BMRI", "Bank Mandiri (Persero) Tbk", "Perbankan"),
        StockTickerItem("BBNI", "Bank Negara Indonesia (Persero) Tbk", "Perbankan"),
        StockTickerItem("BRIS", "Bank Syariah Indonesia Tbk", "Perbankan"),
        StockTickerItem("TLKM", "Telkom Indonesia (Persero) Tbk", "Telekomunikasi"),
        StockTickerItem("ASII", "Astra International Tbk", "Otomotif & Industri"),
        StockTickerItem("GOTO", "GoTo Gojek Tokopedia Tbk", "Teknologi"),
        StockTickerItem("AMMN", "Amman Mineral Internasional Tbk", "Pertambangan"),
        StockTickerItem("ANTM", "Aneka Tambang Tbk", "Pertambangan"),
        StockTickerItem("ICBP", "Indofood CBP Sukses Makmur Tbk", "Konsumsi"),
        StockTickerItem("INDF", "Indofood Sukses Makmur Tbk", "Konsumsi"),
        StockTickerItem("UNVR", "Unilever Indonesia Tbk", "Konsumsi"),
        StockTickerItem("ADRO", "Adaro Energy Indonesia Tbk", "Energi"),
        StockTickerItem("PGAS", "Perusahaan Gas Negara Tbk", "Energi"),
        StockTickerItem("KLBF", "Kalbe Farma Tbk", "Kesehatan"),
        StockTickerItem("MDKA", "Merdeka Copper Gold Tbk", "Pertambangan"),
        StockTickerItem("ACES", "Aspirasi Hidup Indonesia Tbk", "Ritel"),
        StockTickerItem("CPIN", "Charoen Pokphand Indonesia Tbk", "Agrikultur"),
        StockTickerItem("SMGR", "Semen Indonesia (Persero) Tbk", "Semen"),
        StockTickerItem("INCO", "Vale Indonesia Tbk", "Nikel"),
        StockTickerItem("PTBA", "Bukit Asam Tbk", "Batubara"),
        StockTickerItem("MEDC", "Medco Energi Internasional Tbk", "Minyak & Gas"),
        StockTickerItem("BUKA", "Bukalapak.com Tbk", "Teknologi"),
        StockTickerItem("ARTO", "Bank Jago Tbk", "Bank Digital"),
        StockTickerItem("TPIA", "Chandra Asri Pacific Tbk", "Petrokimia"),
        StockTickerItem("BREN", "Barito Renewables Tbk", "Energi Terbarukan"),
        StockTickerItem("CUAN", "Petrindo Jaya Kreasi Tbk", "Pertambangan"),
        StockTickerItem("GGRM", "Gudang Garam Tbk", "Konsumsi"),
        StockTickerItem("HMSP", "HM Sampoerna Tbk", "Konsumsi"),
        StockTickerItem("MYOR", "Mayora Indah Tbk", "Konsumsi")
    )

    fun formatSymbol(input: String): String {
        val trimmed = input.trim().uppercase()
        if (trimmed.isEmpty()) return "IDX:BBRI"
        if (trimmed.contains(":")) return trimmed
        return if (trimmed.length in 3..6 && trimmed.all { it.isLetterOrDigit() }) {
            "IDX:$trimmed"
        } else {
            trimmed
        }
    }

    fun getDisplayName(symbol: String): String {
        val clean = symbol.substringAfter(":")
        val found = popularStocks.firstOrNull { it.code.equals(clean, ignoreCase = true) }
        return if (found != null) "${found.code} - ${found.name}" else symbol
    }

    fun generateAdvancedChartHtml(symbol: String, interval: String = "D"): String {
        val safeSymbol = formatSymbol(symbol)
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
              <style>
                * { box-sizing: border-box; }
                html, body {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  background-color: #12181F;
                  overflow: hidden;
                }
                .tradingview-widget-container {
                  width: 100%;
                  height: 100%;
                }
                #tradingview_chart {
                  width: 100%;
                  height: 100%;
                }
              </style>
            </head>
            <body>
              <div class="tradingview-widget-container">
                <div id="tradingview_chart"></div>
                <script type="text/javascript">
                  window.onerror = function(msg, url, line) {
                    console.log("TV log: " + msg);
                    return true;
                  };
                </script>
                <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                <script type="text/javascript">
                  function initTV() {
                    try {
                      if (typeof TradingView !== 'undefined' && TradingView.widget) {
                        new TradingView.widget({
                          "autosize": true,
                          "symbol": "$safeSymbol",
                          "interval": "$interval",
                          "timezone": "Asia/Jakarta",
                          "theme": "dark",
                          "style": "1",
                          "locale": "id",
                          "toolbar_bg": "#12181F",
                          "enable_publishing": false,
                          "allow_symbol_change": true,
                          "container_id": "tradingview_chart",
                          "hide_side_toolbar": false,
                          "withdateranges": true,
                          "details": true,
                          "hotlist": false,
                          "calendar": false,
                          "studies": [
                            "MASimple@tv-basicstudies",
                            "RSI@tv-basicstudies"
                          ]
                        });
                      }
                    } catch (e) {
                      console.log("Error initializing widget: " + e);
                    }
                  }
                  if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', initTV);
                  } else {
                    initTV();
                  }
                </script>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun generateMiniChartHtml(symbol: String): String {
        val safeSymbol = formatSymbol(symbol)
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
              <style>
                * { box-sizing: border-box; }
                html, body {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  background-color: #12181F;
                  overflow: hidden;
                }
                .tradingview-widget-container {
                  width: 100%;
                  height: 100%;
                }
                .tradingview-widget-container__widget {
                  width: 100%;
                  height: 100%;
                }
              </style>
            </head>
            <body>
              <div class="tradingview-widget-container">
                <div class="tradingview-widget-container__widget"></div>
                <script type="text/javascript" src="https://s3.tradingview.com/external-embedding/embed-widget-symbol-overview.js" async>
                {
                  "symbols": [
                    [
                      "$safeSymbol|1D"
                    ]
                  ],
                  "chartOnly": false,
                  "width": "100%",
                  "height": "100%",
                  "locale": "id",
                  "colorTheme": "dark",
                  "autosize": true,
                  "showVolume": true,
                  "showMA": true,
                  "hideDateRanges": false,
                  "hideMarketStatus": false,
                  "hideSymbolLogo": false,
                  "scalePosition": "right",
                  "scaleMode": "Normal",
                  "fontFamily": "-apple-system, BlinkMacSystemFont, Trebuchet MS, Roboto, Ubuntu, sans-serif",
                  "fontSize": "10",
                  "noTimeScale": false,
                  "valuesTracking": "1",
                  "changeMode": "price-and-percent",
                  "chartType": "area",
                  "maLineColor": "#3B82F6",
                  "maLineWidth": 1,
                  "maLength": 9,
                  "lineWidth": 2,
                  "lineType": 0,
                  "dateRanges": [
                    "1d|1",
                    "1m|30",
                    "3m|60",
                    "12m|1D",
                    "all|1M"
                  ]
                }
                </script>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun getExternalTradingViewUrl(symbol: String): String {
        val safeSymbol = formatSymbol(symbol).replace(":", "-")
        return "https://id.tradingview.com/symbols/$safeSymbol/"
    }
}
