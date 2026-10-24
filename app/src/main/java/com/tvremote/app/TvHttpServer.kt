package com.tvremote.app

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

class TvHttpServer(private val context: Context, private val port: Int = 8080) : NanoHTTPD(port) {

    private val service get() = RemoteAccessibilityService.instance

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return when {
            uri == "/" && method == Method.GET -> serveHtml()
            uri == "/api/key" && method == Method.POST -> handleKey(session)
            uri == "/api/text" && method == Method.POST -> handleText(session)
            uri == "/api/app" && method == Method.POST -> handleApp(session)
            uri == "/api/status" && method == Method.GET -> handleStatus()
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }

    private fun serveHtml(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>TV Remote</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        background: #0a0a0a;
                        color: #e0e0e0;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        padding: 20px;
                    }
                    h1 { font-size: 24px; margin-bottom: 20px; color: #00ff88; }
                    .status {
                        padding: 10px 20px;
                        border-radius: 8px;
                        margin-bottom: 30px;
                        font-size: 14px;
                        background: #1a1a1a;
                        border: 1px solid #333;
                    }
                    .status.ready { color: #00ff88; border-color: #00ff88; }
                    .status.error { color: #ff4444; border-color: #ff4444; }

                    .dpad {
                        display: grid;
                        grid-template-columns: 80px 80px 80px;
                        grid-template-rows: 80px 80px 80px;
                        gap: 8px;
                        margin-bottom: 20px;
                    }
                    .btn {
                        background: #1a1a1a;
                        border: 2px solid #333;
                        border-radius: 12px;
                        color: #fff;
                        font-size: 24px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: all 0.2s;
                        user-select: none;
                        -webkit-user-select: none;
                    }
                    .btn:active { background: #00ff88; color: #000; transform: scale(0.95); }
                    .btn-up { grid-column: 2; grid-row: 1; }
                    .btn-left { grid-column: 1; grid-row: 2; }
                    .btn-center { grid-column: 2; grid-row: 2; font-size: 18px; background: #333; }
                    .btn-right { grid-column: 3; grid-row: 2; }
                    .btn-down { grid-column: 2; grid-row: 3; }

                    .row {
                        display: flex;
                        gap: 10px;
                        margin-bottom: 10px;
                        flex-wrap: wrap;
                        justify-content: center;
                    }
                    .btn-small {
                        background: #1a1a1a;
                        border: 1px solid #333;
                        border-radius: 8px;
                        color: #e0e0e0;
                        font-size: 14px;
                        padding: 12px 16px;
                        cursor: pointer;
                        min-width: 70px;
                        text-align: center;
                        user-select: none;
                        -webkit-user-select: none;
                    }
                    .btn-small:active { background: #00ff88; color: #000; }
                    .btn-small.primary { background: #00aa44; border-color: #00aa44; color: #fff; }
                    .btn-small.danger { background: #aa2222; border-color: #aa2222; color: #fff; }

                    .text-input {
                        width: 100%;
                        max-width: 400px;
                        padding: 12px;
                        background: #1a1a1a;
                        border: 1px solid #333;
                        border-radius: 8px;
                        color: #fff;
                        font-size: 16px;
                        margin-bottom: 10px;
                    }
                    .text-input:focus { outline: none; border-color: #00ff88; }

                    .log {
                        margin-top: 20px;
                        padding: 10px;
                        background: #1a1a1a;
                        border: 1px solid #333;
                        border-radius: 8px;
                        font-size: 12px;
                        color: #888;
                        max-width: 400px;
                        width: 100%;
                        min-height: 60px;
                        max-height: 120px;
                        overflow-y: auto;
                    }
                    .log div { margin: 2px 0; }
                    .log .success { color: #00ff88; }
                    .log .error { color: #ff4444; }
                </style>
            </head>
            <body>
                <h1>📺 TV Remote</h1>
                <div id="status" class="status error">CHECKING...</div>

                <div class="dpad">
                    <div class="btn btn-up" onclick="sendKey(19)">▲</div>
                    <div class="btn btn-left" onclick="sendKey(21)">◀</div>
                    <div class="btn btn-center" onclick="sendKey(23)">OK</div>
                    <div class="btn btn-right" onclick="sendKey(22)">▶</div>
                    <div class="btn btn-down" onclick="sendKey(20)">▼</div>
                </div>

                <div class="row">
                    <div class="btn-small" onclick="sendKey(4)">Back</div>
                    <div class="btn-small" onclick="sendKey(3)">Home</div>
                    <div class="btn-small" onclick="sendKey(82)">Menu</div>
                </div>
                <div class="row">
                    <div class="btn-small" onclick="sendKey(24)">Vol+</div>
                    <div class="btn-small" onclick="sendKey(25)">Vol-</div>
                    <div class="btn-small" onclick="sendKey(164)">Mute</div>
                </div>
                <div class="row">
                    <div class="btn-small primary" onclick="launch('com.mediatek.wwtv.tvcenter')">TV</div>
                    <div class="btn-small primary" onclick="launch('com.google.android.apps.youtube.tv')">YT</div>
                    <div class="btn-small primary" onclick="launch('com.netflix.ninja')">NETFLIX</div>
                </div>
                <div class="row">
                    <div class="btn-small" onclick="sendKey(26)">Power</div>
                    <div class="btn-small" onclick="sendKey(85)">Play</div>
                    <div class="btn-small" onclick="sendKey(86)">Pause</div>
                </div>

                <input type="text" id="textInput" class="text-input" placeholder="Ketik teks untuk search...">
                <div class="row">
                    <div class="btn-small primary" onclick="sendText()">Kirim Teks</div>
                    <div class="btn-small" onclick="document.getElementById('textInput').value=''">Clear</div>
                </div>

                <div id="log" class="log"></div>

                <script>
                    const logEl = document.getElementById('log');
                    function log(msg, type) {
                        const div = document.createElement('div');
                        div.className = type || '';
                        div.textContent = new Date().toLocaleTimeString() + ' - ' + msg;
                        logEl.prepend(div);
                        if (logEl.children.length > 20) logEl.lastChild.remove();
                    }

                    async function checkStatus() {
                        try {
                            const r = await fetch('/api/status');
                            const data = await r.json();
                            const el = document.getElementById('status');
                            if (data.service) {
                                el.textContent = 'Status: READY';
                                el.className = 'status ready';
                            } else {
                                el.textContent = 'Status: SERVICE OFF';
                                el.className = 'status error';
                            }
                        } catch (e) {
                            document.getElementById('status').textContent = 'Status: ERROR';
                            document.getElementById('status').className = 'status error';
                        }
                    }

                    async function sendKey(code) {
                        try {
                            const r = await fetch('/api/key', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({keycode: code})
                            });
                            const data = await r.json();
                            log(data.ok ? 'Key sent: ' + code : 'Key failed', data.ok ? 'success' : 'error');
                        } catch (e) {
                            log('Error: ' + e.message, 'error');
                        }
                    }

                    async function sendText() {
                        const text = document.getElementById('textInput').value;
                        if (!text) return;
                        try {
                            const r = await fetch('/api/text', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({text: text})
                            });
                            const data = await r.json();
                            log(data.ok ? 'Text sent' : 'Text failed', data.ok ? 'success' : 'error');
                        } catch (e) {
                            log('Error: ' + e.message, 'error');
                        }
                    }

                    async function launch(pkg) {
                        try {
                            const r = await fetch('/api/app', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({package: pkg})
                            });
                            const data = await r.json();
                            log(data.ok ? 'Launched: ' + pkg : 'Launch failed', data.ok ? 'success' : 'error');
                        } catch (e) {
                            log('Error: ' + e.message, 'error');
                        }
                    }

                    checkStatus();
                    setInterval(checkStatus, 3000);
                </script>
            </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, html)
    }

    private fun handleKey(session: IHTTPSession): Response {
        val params = mutableMapOf<String, String>()
        session.parseBody(params)
        val body = params["postData"] ?: return error("No data")

        return try {
            val json = JSONObject(body)
            val keyCode = json.getInt("keycode")
            val ok = service?.sendKey(keyCode) ?: false
            jsonResponse(mapOf("ok" to ok))
        } catch (e: Exception) {
            error("Bad request: ${e.message}")
        }
    }

    private fun handleText(session: IHTTPSession): Response {
        val params = mutableMapOf<String, String>()
        session.parseBody(params)
        val body = params["postData"] ?: return error("No data")

        return try {
            val json = JSONObject(body)
            val text = json.getString("text")
            val ok = service?.sendText(text) ?: false
            jsonResponse(mapOf("ok" to ok))
        } catch (e: Exception) {
            error("Bad request: ${e.message}")
        }
    }

    private fun handleApp(session: IHTTPSession): Response {
        val params = mutableMapOf<String, String>()
        session.parseBody(params)
        val body = params["postData"] ?: return error("No data")

        return try {
            val json = JSONObject(body)
            val pkg = json.getString("package")
            val ok = service?.launchApp(pkg) ?: false
            jsonResponse(mapOf("ok" to ok))
        } catch (e: Exception) {
            error("Bad request: ${e.message}")
        }
    }

    private fun handleStatus(): Response {
        val ok = service != null
        return jsonResponse(mapOf("service" to ok, "port" to port))
    }

    private fun jsonResponse(data: Map<String, Any>): Response {
        val json = JSONObject(data).toString()
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json)
    }

    private fun error(msg: String): Response {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, msg)
    }
}
