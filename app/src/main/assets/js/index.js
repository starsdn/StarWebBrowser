var ser_ip, ser_port, hphm, hpzl, zpzl, xsnr,initMsg;
//配置服务IP
function configIp(el) {
    if ($(el).hasClass('active')) {
        window.WebViewJavascriptBridge.callHandler(
            'configIp', { 'param': '中文测试' }
            , function (responseData) {
                //   var ss = responseData.ip;
                //   alert(ss);
                var json_resp = JSON.parse(responseData);
                ser_ip = json_resp["ip"]; //得到对应的Ip
                ser_port = json_resp["port"];//得到对应的prot
                //  alert(ser_ip);
            }
        );
    } else {
        alert("无权限使用");
    }

}
//摄像头拍照，打开拍照页面
function OpenCamear(el) {
    if ($(el).hasClass("active")) {
        window.WebViewJavascriptBridge.callHandler(
            'ShotCamera', { 'code': '01', 'codename': 'xxxxxx', 'clsbdh': '2332233232' },
            function (responseData) {
                //  document.getElementById("show").innerHTML = "send " + responseData;
            }
        );
        $(el).removeClass('active');
    }
}
//打开录像
function OpenRecVideo(el) {
    if ($(el).hasClass("active")) {
        window.WebViewJavascriptBridge.callHandler(
            'RecVideo', { 'param': '2222' },
            function (responseData) {
                //document.getElementById("show").innerHTML = "send " + responseData;
            }
        );
        $(el).removeClass('active');
    }

}
//中止检测
function tmlStop(el) {
    //alert("tml");
    if ($(el).hasClass('active')) {
        var strStop = '{"type":"tml","data":""}';
        $.ajax({
            url: "http://" + ser_ip + ":" + ser_port,
            type: "POST",
            //dataType: "application/json;charset=utf-8",
            dataType:'JSON',
            data: strStop,
            success: function (data) {
                //   alert(data);
                $("#tml_stop").removeClass('active');
                $("#end_stop").removeClass('active');
                $("#camera").removeClass('active');
                $("#recvideo").removeClass('active');
                $("#hphm").text("车牌")
                $("#hphm").parent("div").removeClass('active');//改变样式
                window.WebViewJavascriptBridge.callHandler(
                    'cleanUp', { 'param': '2222' },
                    function (responseData) {
                       // document.getElementById("show").innerHTML = "send " + responseData;
                       hphm="";
                       hpzl="";
                       zpzl="";
                       xsnr="";
                       $("#tips").text("外观检测终止，请等待新指令");//显示 拍照内容
                    }
                );
            },
            error: function (req, status, info) {
                //   alert(req.statusCode);
                var error_info = '{"content":"请求服务错误，'+status+',请检测网络设置","type":"3"}'
                htmlShowLogs(error_info);
                $("#line_status").removeClass('active');
                $("#line_status").children("div:last").text('离线');
            }
        })
    }
}
//结束
function endStop(el) {
    //alert("end");
    if ($(el).hasClass('active')) {
        var strStop = '{"type":"end","data":""}';
      //  alert(strStop);
        $.ajax({
            url: "http://" + ser_ip + ":" + ser_port,
            type: "POST",
            //dataType: "application/json;charset=utf-8",
            dataType:'JSON',
            data: strStop,
            success: function (data) {
                //   alert(data);
                $("#tml_stop").removeClass('active');
                $("#end_stop").removeClass('active');
                $("#camera").removeClass('active');
                $("#recvideo").removeClass('active');
                $("#hphm").text("车牌")
                $("#hphm").parent("div").removeClass('active');//改变样式
                window.WebViewJavascriptBridge.callHandler(
                    'cleanUp', { 'param': '2222' },
                    function (responseData) {
                       // document.getElementById("show").innerHTML = "send " + responseData;
                       hphm="";
                       hpzl="";
                       zpzl="";
                       xsnr="";
                       $("#tips").text("外观检测完成，请等待新指令");//显示 拍照内容
                    }
                );
            },
            error: function (req, status, info) {
                //   alert(req.statusCode);
                var error_info = '{"content":"请求服务错误，'+status+',请检测网络设置","type":"3"}'
                htmlShowLogs(error_info);
                $("#line_status").removeClass('active');
                $("#line_status").children("div:last").text('离线');
            }
        })
    }
}
//尝试重新连接服务器
function reConn(el){
    if($(el).hasClass('active')){
        return;
    }
    var error_info = '{"content":"重新连接服务器……","type":"0"}'
    htmlShowLogs(error_info);
    $.ajax({
        url: "http://" + ser_ip + ":" + ser_port,
        type: "POST",
        //dataType: "application/json;charset=utf-8",
        dataType:'JSON',
        data: initMsg,
        success: function (data) {
            //   alert(data);
            $("#line_status").addClass('active');
            $("#line_status").children("div:last").text('在线');
        },
        error: function (req, status, info) {
           // alert(req.statusCode);
           // alert(status);
            var error_info = '{"content":"请求服务错误，'+status+',请检测网络设置","type":"3"}'
            htmlShowLogs(error_info);
           // alert(info);
            $("#line_status").removeClass('active');
            $("#line_status").children("div:last").text('离线');
        }
    })
}
//显示tips信息
function tipsShow(data){
    $("#tips").text(data+"");//显示 拍照内容
}

function connectWebViewJavascriptBridge(callback) {
    if (window.WebViewJavascriptBridge) {
        callback(WebViewJavascriptBridge)
    } else {
        document.addEventListener(
            'WebViewJavascriptBridgeReady'
            , function () {
                callback(WebViewJavascriptBridge)
            },
            false
        );
    }
}

connectWebViewJavascriptBridge(function (bridge) {
    bridge.init(function (message, responseCallback) {
        //alert(message);
        initMsg = message;//全局存储初始化变量
        //  console.log('JS得到信息', message);
        //得到对应的初始化值
        var json_data = JSON.parse(message).data;//得到初始化传递过来的json
        ser_ip = json_data.ip;//服务端Ip
        ser_port = json_data.port;//服务端port
        if (!ser_port || !ser_ip) {//如果没有配置IP
            window.WebViewJavascriptBridge.callHandler(
                'configIp', { 'param': 'test' }
                , function (responseData) {
                    //   var ss = responseData.ip;
                    //   alert(ss);
                    var json_resp = JSON.parse(responseData);
                    ser_ip = json_resp["ip"]; //得到对应的Ip
                    ser_port = json_resp["port"];//得到对应的prot
                    //  alert(ser_ip);
                }
            );
            return;
        }

        if (json_data && json_data.hphm && json_data.hphm != "") {
            hphm = json_data.hphm;
            hpzl = json_data.hpzl;
            zpzl = json_data.zpzl; //录像类型
            var xsnr = json_data.xsnr;//显示内容
            var lx = json_data.lx; //录像类型
            var init_logs = '{"content":"存在未完成的拍照项","type":"2"}';
            htmlShowLogs(init_logs);
           // var init_tips_1 = (lx == "0" ? "请拍照:" : "请录像:") + " " + xsnr;
           var init_tips;
            //alert(init_tips_1);
            if (lx == "0") {//照片
                init_tips = "请拍照："+xsnr;
                $('#camera').addClass('active'); //可拍照
                $('#recvideo').removeClass('active'); //不可录像
            } else {
                init_tips = "请录像："+xsnr;
                $('#camera').removeClass('active'); //不可拍照
                $('#recvideo').addClass('active'); //可录像
            }           
            $("#hphm").text(hphm); //给车牌赋值
            $("#hphm").parent("div").addClass('active');//改变样式
            $("#tml_stop").addClass('active');
           // $("#end_stop").addClass('active');
           // htmlShowTips('{"xsnr":"' + init_tips_1 + '"}');
           $('#tips').text(init_tips);

        } else {
            var init_logs = '{"content":"初始化成功","type":"1"}';
            htmlShowLogs(init_logs);
            $('#tips').text("请等待外观检测指令");
        }
        $.ajax({
            url: "http://" + ser_ip + ":" + ser_port,
            type: "POST",
            //dataType: "application/json;charset=utf-8",
            dataType:'JSON',
            data: message,
            success: function (data) {
                //   alert(data);
                $("#line_status").addClass('active');
                $("#line_status").children("div:last").text('在线');
            },
            error: function (req, status, info) {
               // alert(req.statusCode);
               // alert(status);
                var error_info = '{"content":"请求服务错误，'+status+',请检测网络设置","type":"3"}'
                htmlShowLogs(error_info);
               // alert(info);
                $("#line_status").removeClass('active');
                $("#line_status").children("div:last").text('离线');
            }
        })
        // var data = { "response": "sdnsdn" };
        // if (responseCallback) {
        //     console.log('JS 请求', data);
        //     responseCallback(data);
        // }
    });
    //显示日志
    bridge.registerHandler("js_show_log", function (data, responseCallback) {
        //  alert(data);
        //document.getElementById("show").innerHTML = data;
        htmlShowLogs(data);
        if (responseCallback) {
            var responseData = "得到对应的结果";
            responseCallback(responseData);
        }
    });
    //显示拍照内容
    bridge.registerHandler("js_show_tip", function (data, responseCallback) {
        // alert(data);
        //document.getElementById("show").innerHTML = data;
        htmlShowTips(data);

        if (responseCallback) {
            var responseData = "得到对应的结果";
            responseCallback(responseData);
        }
    });
    bridge.registerHandler("show_tip_only", function (data, responseCallback) {
        // alert(data);
        //document.getElementById("show").innerHTML = data;
        tipsShow(data);
        $("#end_stop").addClass('active');   //停止检测可用
        if (responseCallback) {
            var responseData = "得到对应的结果";
            responseCallback(responseData);
        }
    });

})

//显示日志内容
function htmlShowLogs(data) {
    var json_data = JSON.parse(data);
    var logs_num = $("#logs").children().length;
    if (logs_num > 8) {
        //如果日志行数大于8 则删除第一条
        $("#logs").children().first().remove();
    }
    var logs_div = "<div class='logs_item'>" + json_data["content"] + "</div>";
    if (json_data.type == "0") //黑色
    {
        logs_div = "<div class='logs_item'>" + json_data["content"] + "</div>";
    } else if (json_data.type == "1") {//绿色
        logs_div = "<div class='logs_item success'>" + json_data["content"] + "</div>";
    } else if (json_data.type == "2") //黄色
    {
        logs_div = "<div class='logs_item waring'>" + json_data["content"] + "</div>";
    } else {
        logs_div = "<div class='logs_item error'>" + json_data["content"] + "</div>";
         $("#recvideo").removeClass("active"); //录像不可用
    }
    $("#logs").append(logs_div);
}

//显示拍照tips
function htmlShowTips(data) {
    //   alert("tips");
    var json_data = JSON.parse(data);
    if (json_data.lx == "0") { //拍照
        hphm = json_data.hphm;
        hpzl = json_data.hpzl;
        zpzl = json_data.zpzl; //录像类型
        $("#tips").text("请拍照："+json_data.xsnr);//显示 拍照内容
        $("#camera").addClass("active"); //拍照可用
        $("#recvideo").removeClass("active"); //录像不可用   
        $("#tml_stop").addClass('active'); //中止检测可用
      //  $("#end_stop").addClass('active');   //停止检测可用
        $("#line_status").addClass('active');
        $("#line_status").children("div:last").text('在线');//离线状态 在线
        $("#hphm").text(hphm); //给车牌赋值
        $("#hphm").parent("div").addClass('active');//改变样式

    } else {//录像
        hphm = json_data.hphm;
        hpzl = json_data.hpzl;
        zpzl = json_data.zpzl; //录像类型
        $("#tips").text("请录像："+json_data.xsnr);//显示 拍照内容
        $("#camera").removeClass("active"); //拍照不可用
        $("#recvideo").addClass("active");//录像不可用
        $("#tml_stop").addClass('active'); //中止检测可用
      //  $("#end_stop").addClass('active'); //停止检测可用
        $("#line_status").addClass('active');
        $("#line_status").children("div:last").text('在线');//离线状态 在线
        $("#hphm").text(hphm); //给车牌赋值
        $("#hphm").parent("div").addClass('active');//改变样式
    }
}
