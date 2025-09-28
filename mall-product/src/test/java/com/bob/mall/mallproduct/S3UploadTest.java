package com.bob.mall.mallproduct;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bob.mall.product.MallProductApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest(classes = MallProductApplication.class)
public class S3UploadTest {

    @Autowired
    private AmazonS3 s3;

    @Test
    public void testUploadFile() {

        String bucketName = "e-mall-files";
        String keyName = "e-mall-architecure-2.png";
        String filePath = "C:\\Users\\BOB\\Desktop\\E-mall architecture.png";

        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, new File(filePath));

        s3.putObject(request);

        System.out.println("Check the file on AWS S3");

    }
}
