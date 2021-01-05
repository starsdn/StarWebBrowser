(function(doc,win,designSize){
    var docEl = doc.documentElement,
        resizeEnv = 'orientationchange' in window ? 'orientationchange' : 'resize',
        recalc = function() {
            var clientWidth = docEl.clientWidth;
            if(!clientWidth) return;
            docEl.style.fontSize = 100*(clientWidth / designSize) + 'px'
        };
        if(!doc.addEventListener) return;
        win.addEventListener(resizeEnv,recalc,false);
        doc.addEventListener('DOMContentLoaded',recalc,false)
})(document,window,1334)