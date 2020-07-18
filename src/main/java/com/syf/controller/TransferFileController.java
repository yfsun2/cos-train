package com.syf.controller;

import com.syf.utils.CosUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author syf
 * @Date 2020/7/17 13:45
 */
@RestController
public class TransferFileController {

    @RequestMapping("/upload/img")
    public String uploadImg(@RequestParam(value = "img") MultipartFile multipartFile) throws IOException, InterruptedException {

        return CosUtils.uploadFile(multipartFile, "image");
    }

    @RequestMapping("/upload/video")
    public String uploadVideo(@RequestParam(value = "video") MultipartFile multipartFile) throws IOException, InterruptedException {
        return CosUtils.uploadFile(multipartFile, "video");
    }

    @RequestMapping("/upload/doc")
    public String uploadDoc(@RequestParam(value = "video") MultipartFile multipartFile) throws IOException, InterruptedException {
        return CosUtils.uploadFile(multipartFile, "document");
    }
}
