package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

//通用接口
@RestController
@RequestMapping("/admin/common")
@ApiOperation("通用接口")
@Slf4j
public class CommonController {
    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}",file);
        try {
            String originalFilename = file.getOriginalFilename();//原始文件名xxx.png
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//截取扩展名
            //构造新文件名称
            String objectName = UUID.randomUUID().toString() + suffix;
            //文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);

    }
}
