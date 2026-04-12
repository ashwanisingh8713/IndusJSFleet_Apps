package com.indusjs.fleet.core.debug

// #region agent log
actual fun postDebugLog9fbb5d(json: String) {
    js("""
        try {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://127.0.0.1:7630/ingest/1c7c0c7a-9670-44ca-bd55-6adf25dc35b9', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.setRequestHeader('X-Debug-Session-Id', '9fbb5d');
            xhr.send(json);
        } catch(e) { console.warn('[DBG_9fbb5d] post failed', e); }
    """)
}
// #endregion
