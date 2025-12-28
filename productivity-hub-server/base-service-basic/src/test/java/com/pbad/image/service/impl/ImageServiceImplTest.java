package com.pbad.image.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.image.config.ImageProperties;
import com.pbad.image.constants.ImageErrorCode;
import com.pbad.image.domain.dto.ImageUpdateDTO;
import com.pbad.image.domain.po.ImagePO;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.mapper.ImageMapper;
import com.pbad.image.service.ImageCompressService;
import com.pbad.image.service.ImageStorageService;
import com.pbad.image.service.ImageThumbnailService;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 图片核心业务服务单元测试.
 * 关联测试用例：TC-IMG-001至TC-IMG-010
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ImageCompressService imageCompressService;

    @Mock
    private ImageThumbnailService imageThumbnailService;

    @Mock
    private ImageProperties imageProperties;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageServiceImpl imageService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_IMAGE_ID = "img_001";
    private static final String TEST_FILE_PATH = "/uploads/images/2025/01/test.jpg";
    private static final String TEST_THUMBNAIL_PATH = "/uploads/images/2025/01/thumb_test.jpg";

    @BeforeEach
    void setUp() {
        when(idGeneratorApi.generateId()).thenReturn(TEST_IMAGE_ID);
        when(imageProperties.getMaxFileSize()).thenReturn(5 * 1024 * 1024L); // 5MB
        when(imageProperties.getBatchMaxCount()).thenReturn(10);
        when(imageProperties.getAllowedTypes()).thenReturn(Arrays.asList("image/jpeg", "image/png", "image/gif"));
        when(imageProperties.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));
    }

    /**
     * TC-IMG-001: 正常上传图片
     */
    @Test
    void testUploadImage_Success() throws IOException {
        // 准备测试数据
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage thumbnail = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        byte[] thumbnailBytes = new byte[]{1, 2, 3};

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(imageStorageService.saveImage(anyString(), anyString(), any(byte[].class))).thenReturn(TEST_FILE_PATH);
        when(imageStorageService.saveThumbnail(anyString(), anyString(), any(byte[].class))).thenReturn(TEST_THUMBNAIL_PATH);
        when(imageStorageService.generateFileUrl(TEST_FILE_PATH)).thenReturn("http://localhost" + TEST_FILE_PATH);
        when(imageStorageService.generateThumbnailUrl(TEST_THUMBNAIL_PATH)).thenReturn("http://localhost" + TEST_THUMBNAIL_PATH);
        when(imageThumbnailService.generateThumbnail(any(BufferedImage.class))).thenReturn(thumbnail);
        when(imageMapper.insert(any(ImagePO.class))).thenReturn(1);

        // 执行测试
        ImageVO result = imageService.uploadImage(multipartFile, "avatar", "user", "user_001", "测试图片", TEST_USER_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_IMAGE_ID, result.getId());
        verify(imageMapper, times(1)).insert(any(ImagePO.class));
    }

    /**
     * TC-IMG-002: 上传图片-文件为空
     */
    @Test
    void testUploadImage_FileEmpty() {
        when(multipartFile.isEmpty()).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            imageService.uploadImage(multipartFile, "avatar", null, null, null, TEST_USER_ID);
        });

        assertEquals(ImageErrorCode.INVALID_PARAMETER, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("请选择要上传的图片文件"));
    }

    /**
     * TC-IMG-003: 上传图片-不支持的文件类型
     */
    @Test
    void testUploadImage_UnsupportedFileType() throws IOException {
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            imageService.uploadImage(multipartFile, "avatar", null, null, null, TEST_USER_ID);
        });

        assertEquals(ImageErrorCode.UNSUPPORTED_FILE_TYPE, exception.getErrorCode());
    }

    /**
     * TC-IMG-004: 批量上传图片-正常上传
     */
    @Test
    void testBatchUploadImages_Success() throws IOException {
        MultipartFile[] files = new MultipartFile[]{multipartFile};
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage thumbnail = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(imageStorageService.saveImage(anyString(), anyString(), any(byte[].class))).thenReturn(TEST_FILE_PATH);
        when(imageStorageService.saveThumbnail(anyString(), anyString(), any(byte[].class))).thenReturn(TEST_THUMBNAIL_PATH);
        when(imageStorageService.generateFileUrl(TEST_FILE_PATH)).thenReturn("http://localhost" + TEST_FILE_PATH);
        when(imageStorageService.generateThumbnailUrl(TEST_THUMBNAIL_PATH)).thenReturn("http://localhost" + TEST_THUMBNAIL_PATH);
        when(imageThumbnailService.generateThumbnail(any(BufferedImage.class))).thenReturn(thumbnail);
        when(imageMapper.insert(any(ImagePO.class))).thenReturn(1);

        List<ImageVO> result = imageService.batchUploadImages(files, "avatar", null, null, null, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * TC-IMG-005: 批量上传图片-数量超过限制
     */
    @Test
    void testBatchUploadImages_ExceedLimit() {
        MultipartFile[] files = new MultipartFile[11]; // 超过限制

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            imageService.batchUploadImages(files, "avatar", null, null, null, TEST_USER_ID);
        });

        assertEquals(ImageErrorCode.BATCH_LIMIT_EXCEEDED, exception.getErrorCode());
    }

    /**
     * TC-IMG-006: 查询图片详情-正常查询
     */
    @Test
    void testGetImageById_Success() {
        ImagePO imagePO = createTestImagePO();
        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);

        ImageVO result = imageService.getImageById(TEST_IMAGE_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_IMAGE_ID, result.getId());
    }

    /**
     * TC-IMG-007: 查询图片详情-图片不存在
     */
    @Test
    void testGetImageById_NotFound() {
        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            imageService.getImageById(TEST_IMAGE_ID, TEST_USER_ID);
        });

        assertEquals(ImageErrorCode.IMAGE_NOT_FOUND, exception.getErrorCode());
    }

    /**
     * TC-IMG-008: 更新图片信息-正常更新
     */
    @Test
    void testUpdateImage_Success() {
        ImagePO imagePO = createTestImagePO();
        ImageUpdateDTO updateDTO = new ImageUpdateDTO();
        updateDTO.setDescription("更新后的描述");

        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);
        when(imageMapper.update(any(ImagePO.class))).thenReturn(1);
        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);

        ImageVO result = imageService.updateImage(TEST_IMAGE_ID, updateDTO, TEST_USER_ID);

        assertNotNull(result);
        verify(imageMapper, times(1)).update(any(ImagePO.class));
    }

    /**
     * TC-IMG-009: 删除图片-正常删除
     */
    @Test
    void testDeleteImage_Success() {
        ImagePO imagePO = createTestImagePO();
        imagePO.setStatus("ACTIVE");

        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);
        when(imageMapper.updateStatus(eq(TEST_IMAGE_ID), eq(TEST_USER_ID), eq("DELETED"), any(LocalDateTime.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            imageService.deleteImage(TEST_IMAGE_ID, TEST_USER_ID);
        });

        verify(imageMapper, times(1)).updateStatus(anyString(), anyString(), eq("DELETED"), any(LocalDateTime.class));
    }

    /**
     * TC-IMG-010: 恢复图片-正常恢复
     */
    @Test
    void testRestoreImage_Success() {
        ImagePO imagePO = createTestImagePO();
        imagePO.setStatus("DELETED");

        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);
        when(imageMapper.updateStatus(eq(TEST_IMAGE_ID), eq(TEST_USER_ID), eq("ACTIVE"), any(LocalDateTime.class))).thenReturn(1);
        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);

        ImageVO result = imageService.restoreImage(TEST_IMAGE_ID, TEST_USER_ID);

        assertNotNull(result);
        verify(imageMapper, times(1)).updateStatus(anyString(), anyString(), eq("ACTIVE"), any(LocalDateTime.class));
    }

    /**
     * TC-IMG-011: 恢复图片-状态不是DELETED
     */
    @Test
    void testRestoreImage_InvalidStatus() {
        ImagePO imagePO = createTestImagePO();
        imagePO.setStatus("ACTIVE");

        when(imageMapper.selectByIdAndUserId(TEST_IMAGE_ID, TEST_USER_ID)).thenReturn(imagePO);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            imageService.restoreImage(TEST_IMAGE_ID, TEST_USER_ID);
        });

        assertEquals(ImageErrorCode.INVALID_STATUS_FOR_RESTORE, exception.getErrorCode());
    }

    /**
     * 创建测试用的ImagePO对象
     */
    private ImagePO createTestImagePO() {
        ImagePO imagePO = new ImagePO();
        imagePO.setId(TEST_IMAGE_ID);
        imagePO.setUserId(TEST_USER_ID);
        imagePO.setOriginalFilename("test.jpg");
        imagePO.setStoredFilename("stored_test.jpg");
        imagePO.setFilePath(TEST_FILE_PATH);
        imagePO.setFileUrl("http://localhost" + TEST_FILE_PATH);
        imagePO.setFileSize(1024L);
        imagePO.setFileType("image/jpeg");
        imagePO.setFileExtension("jpg");
        imagePO.setWidth(100);
        imagePO.setHeight(100);
        imagePO.setCategory("avatar");
        imagePO.setStatus("ACTIVE");
        imagePO.setAccessCount(0L);
        imagePO.setCreatedAt(LocalDateTime.now());
        imagePO.setUpdatedAt(LocalDateTime.now());
        return imagePO;
    }
}

