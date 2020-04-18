$(function (){
    $("#uploadForm").submit(upload);
});

function upload(){

    $.ajax({
        url:"http://upload-z2.qiniup.com",
        method:"post",
        processData:false, //不要把表单内容转换成字符串
        contentType:false, //不让jquery设置上传类型，浏览器自动设置，上传的文件的边界
        data:new FormData($("#uploadForm")[0]), // js对象 用来封装表单数据
        success:function (data){
            if(data!=null&&data.code==0){

                // 更新头像的访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"filename":$("input[name='key']").val()},
                    function data(data){
                        data=$.parseJSON(data);
                        if(data.code==0){
                            window.location.reload();
                        }else{
                            alert(data.msg);
                        }
                    }
                );
            }else{
                alert("上传失败");
            }
        }
    }
    );
    //jquery对象是js对象的数组
    // 当不返回false 时，浏览器会尝试提交表单，如果没写action，会有问题
    return false;
}