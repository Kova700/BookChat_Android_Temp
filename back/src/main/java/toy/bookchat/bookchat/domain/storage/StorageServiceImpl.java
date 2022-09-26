package toy.bookchat.bookchat.domain.storage;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import toy.bookchat.bookchat.config.aws.S3Config;
import toy.bookchat.bookchat.domain.storage.exception.ImageUploadToStorageException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService{

    private final AmazonS3Client amazonS3Client;
    private final S3Config s3Config;

    @Override
    public void upload(MultipartFile multipartFile, String fileName) {
        try {
            amazonS3Client.putObject(s3Config.getBucketName(), fileName,multipartFile.getInputStream(), abstractObjectMetadataFrom(multipartFile));
        } catch (SdkClientException | IOException exception) {
            throw new ImageUploadToStorageException(exception.getMessage(), exception.getCause());
        }
    }

    private ObjectMetadata abstractObjectMetadataFrom(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());
        return objectMetadata;
    }

    @Override
    public String getFileUrl(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(s3Config.getImageBucketUrl());
        stringBuilder.append(fileName);
        return stringBuilder.toString();
    }

    /**
     * '날짜 역순' + UUID로 저장 - S3가 prefix를 사용하여 partitioning을 하기 때문에
     */
    @Override
    public String createFileName(String fileExtension) {
        StringBuilder stringBuilder = new StringBuilder();
        String UUIDFileName = UUID.randomUUID().toString();
        stringBuilder.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).reverse();
        stringBuilder.append(UUIDFileName);
        stringBuilder.append(".");
        stringBuilder.append(fileExtension);
        stringBuilder.insert(0, s3Config.getImageFolder());
        return stringBuilder.toString();
    }
}
