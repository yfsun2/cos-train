package com.syf.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.*;
import com.syf.config.CosConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @Author syf
 * @Date 2020/7/16 16:38
 */
@Component
@ConfigurationProperties(prefix = "cos")
@Data
@Slf4j
public class CosUtils {

    private static String secretId ;

    private static String secretKey;

    private static String localhost ;

    private static String bucketName;

    private static String region;

    private CosUtils(){}

    private static COSClient initCos(){
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }

    private static void showTransferProgress(Transfer transfer) throws InterruptedException  {
        log.info(transfer.getDescription());
        do {
            Thread.sleep(1000);
            TransferProgress progress = transfer.getProgress();
            long soFar = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            log.info("{}/{}", soFar, total);
            log.info("进度:"+ (int) (pct) +"%");
        } while (!transfer.isDone());
        log.info(String.valueOf(transfer.getState()));
    }

    private static ExecutorService initThreadPool(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("thread-call-runner-%d").build();
        return new ThreadPoolExecutor(10,20,200L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),namedThreadFactory);
    }

    private static File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file=File.createTempFile("tmp",null);
        multipartFile.transferTo(file);
        file.deleteOnExit();
        return file;
    }

    public static void uploadFile(String fileName,String basePath) throws InterruptedException{

        COSClient cosclient=initCos();
        TransferManager transferManager = new TransferManager(cosclient, initThreadPool());
        File localFile = new File(fileName);
        String key = basePath+"/" + UUID.randomUUID()+localFile.getName().substring(localFile.getName().lastIndexOf("."));
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        try {
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            long startTime = System.currentTimeMillis();
            Upload upload = transferManager.upload(putObjectRequest);
            showTransferProgress(upload);
            UploadResult uploadResult = upload.waitForUploadResult();
            long endTime = System.currentTimeMillis();
            log.info("上传总共用时: " + (endTime - startTime) / 1000+"秒");
            log.info(localhost +"/"+uploadResult.getKey());
        } catch (CosClientException e) {
            e.printStackTrace();
        }finally {
            transferManager.shutdownNow();
            cosclient.shutdown();
        }
    }

    public static String uploadFile(MultipartFile localFile, String basePath) throws InterruptedException, IOException {

        COSClient cosclient=initCos();
        File file=multipartFileToFile(localFile);
        TransferManager transferManager = new TransferManager(cosclient, initThreadPool());
        String key = basePath+"/" + UUID.randomUUID()+localFile.getOriginalFilename().substring(localFile.getOriginalFilename().lastIndexOf("."));
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
        String url="";
        try {
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            long startTime = System.currentTimeMillis();
            Upload upload = transferManager.upload(putObjectRequest);
            showTransferProgress(upload);
            UploadResult uploadResult = upload.waitForUploadResult();
            long endTime = System.currentTimeMillis();
            log.info("上传总共用时: " + (endTime - startTime) / 1000+"秒");
            log.info(localhost +"/"+uploadResult.getKey());
            url=localhost +"/"+uploadResult.getKey();
            return url;
        } catch (CosClientException e) {
            e.printStackTrace();
        }finally {
            transferManager.shutdownNow();
            cosclient.shutdown();
        }
        return url;
    }

    public static void downloadFile(String key,String fileName) throws InterruptedException {
        COSClient cosclient=initCos();
        TransferManager transferManager = new TransferManager(cosclient, initThreadPool());
        File downloadFile = new File(fileName);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        try {
            long startTime = System.currentTimeMillis();
            Download download = transferManager.download(getObjectRequest, downloadFile);

            download.waitForCompletion();
            long endTime = System.currentTimeMillis();
            log.info("下载总共用时: " + (endTime - startTime) / 1000+"秒");
        } catch (CosClientException e) {
            e.printStackTrace();
        }finally {
            transferManager.shutdownNow();
            cosclient.shutdown();
        }
    }

    public static void initConfigInfo(CosConfig cosConfig) {
        CosUtils.secretId=cosConfig.getSecretId();
        CosUtils.secretKey=cosConfig.getSecretKey();
        CosUtils.localhost=cosConfig.getLocalhost();
        CosUtils.region=cosConfig.getRegion();
        CosUtils.bucketName=cosConfig.getBucketName();
    }
}
