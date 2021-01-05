    var orientation = 0;
    function screenOrientationEvent() {
        if (orientation == 0) {
         //   document.getElementById("change").innerText = "竖";
            $("body").css({
                'transform': 'rotate(90deg)',
                '-webkit-transform': 'rotate(90deg)',
                '-moz-transform': 'rotate(90deg)'
            });
        } else {
         //   document.getElementById("change").innerText = "横";
            $("body").css({
                'transform': 'rotate(0deg)',
                '-webkit-transform': 'rotate(0deg)',
                '-moz-transform': 'rotate(0deg)'
            });
        }
    }
    var innerWidthTmp = window.innerWidth;
    //横竖屏事件监听方法
    function screenOrientationListener() {
        // console.log(1)
        try {
            var iw = window.innerWidth;
            //屏幕方向改变处理
            if (iw != innerWidthTmp) {
                if (iw > window.innerHeight) {
                    orientation = 90;
                } else {
                    orientation = 0;
                }
                //调用转屏事件
                screenOrientationEvent();
                innerWidthTmp = iw;
            }
        } catch (e) { alert(e); };
        //间隔固定事件检查是否转屏，默认500毫秒
        setTimeout("screenOrientationListener()", 500);
    }
    screenOrientationEvent();
    //启动横竖屏事件监听
    screenOrientationListener();