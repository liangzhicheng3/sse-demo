<html>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <div id="sse"></div>
    <script>
        let sse = document.getElementById("sse");
        if(window.EventSource){
            //创建SSE连接
            eventSource = new EventSource(`/createSse?uuid=${uuid}`);
            eventSource.onopen = function(event){
                console.log('SSE连接成功 ...');
            }
            eventSource.onmessage = function(event){
                if(event.data){
                    sse.innerHTML += event.data + '<br/>';
                }
            }
            eventSource.onerror = (error) => {
                console.log('SSE连接失败 ...');
            };
        }else{
            alert("浏览器不支持SSE");
        }
    </script>
</body>
</html>