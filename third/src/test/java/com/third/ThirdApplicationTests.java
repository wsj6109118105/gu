package com.third;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class ThirdApplicationTests {

    @Autowired
    OSS ossClient;

    @Test
    void contextLoads() {
    }

    /**
     * 1.引入starter
     * 2.配置key,secret,endpoint
     * 3.使用OSS
     * @throws FileNotFoundException
     */
    @Test
    void testUpload() throws FileNotFoundException {

        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\wsj\\Pictures\\Camera Roll\\新建文件夹\\1.jpg");
        // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("gudemo", "1.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传完成");
    }
}
