package com.pbad.image.controller;

import com.pbad.image.domain.dto.ImageQueryDTO;
import com.pbad.image.domain.dto.ImageShareDTO;
import com.pbad.image.domain.dto.ImageUpdateDTO;
import com.pbad.image.domain.vo.ImageShareVO;
import com.pbad.image.domain.vo.ImageStatisticsVO;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.service.ImageAccessService;
import com.pbad.image.service.ImageService;
import com.pbad.image.service.ImageShareService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.web.context.RequestUser;
import common.web.context.RequestUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 图片管理控制器单元测试.
 * 关联测试用例：TC-IMG-CONTROLLER-001至TC-IMG-CONTROLLER-020
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @Mock
    private ImageShareService imageShareService;

    @Mock
    private ImageAccessService imageAccessService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageController imageController;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_IMAGE_ID = "img_001";

    @BeforeEach
    void setUp() {
        // 设置用户上下文
        RequestUserContext.set(RequestUser.builder()
                .userId(TEST_USER_ID)
                .username("test_user")
                .build());
    }

    @AfterEach
    void tearDown() {
        // 清理用户上下文
        RequestUserContext.clear();
    }

    // ==================== 图片上传测试 ====================

    /**
     * TC-IMG-CONTROLLER-001: 上传单张图片-成功
     */
    @Test
    void testUploadImage_Success() {
        ImageVO imageVO = createTestImageVO();
        when(imageService.uploadImage(any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.uploadImage(
                multipartFile, "avatar", "user", "user_001", "测试图片");

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("上传成功", response.getMessage());
        assertEquals(imageVO, response.getData());
        verify(imageService, times(1)).uploadImage(any(), anyString(), anyString(), anyString(), anyString(), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-002: 上传单张图片-未登录
     */
    @Test
    void testUploadImage_Unauthorized() {
        RequestUserContext.clear(); // 清除用户上下文

        ApiResponse<ImageVO> response = imageController.uploadImage(
                multipartFile, "avatar", null, null, null);

        assertNotNull(response);
        assertFalse(ApiResponse.isSuccess(response));
        assertEquals("未登录或登录已过期", response.getMessage());
        verify(imageService, never()).uploadImage(any(), any(), any(), any(), any(), any());
    }

    /**
     * TC-IMG-CONTROLLER-003: 批量上传图片-成功
     */
    @Test
    void testBatchUploadImages_Success() {
        MultipartFile[] files = new MultipartFile[]{multipartFile};
        List<ImageVO> imageList = Arrays.asList(createTestImageVO());
        when(imageService.batchUploadImages(any(MultipartFile[].class), anyString(), anyString(), anyString(), anyString(), eq(TEST_USER_ID)))
                .thenReturn(imageList);

        ApiResponse<List<ImageVO>> response = imageController.batchUploadImages(
                files, "avatar", null, null, null);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("批量上传成功", response.getMessage());
        assertEquals(1, response.getData().size());
        verify(imageService, times(1)).batchUploadImages(any(), anyString(), anyString(), anyString(), anyString(), eq(TEST_USER_ID));
    }

    // ==================== 图片查询测试 ====================

    /**
     * TC-IMG-CONTROLLER-004: 分页查询图片列表-成功
     */
    @Test
    void testListImages_Success() {
        ImageQueryDTO queryDTO = new ImageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        PageResult<ImageVO> pageResult = new PageResult<>();
        pageResult.setItems(Arrays.asList(createTestImageVO()));
        pageResult.setTotal(1L);

        when(imageService.listImages(any(ImageQueryDTO.class), eq(TEST_USER_ID)))
                .thenReturn(pageResult);

        ApiResponse<PageResult<ImageVO>> response = imageController.listImages(queryDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(1, response.getData().getItems().size());
        verify(imageService, times(1)).listImages(any(ImageQueryDTO.class), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-005: 查询图片详情-成功
     */
    @Test
    void testGetImageById_Success() {
        ImageVO imageVO = createTestImageVO();
        when(imageService.getImageById(eq(TEST_IMAGE_ID), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.getImageById(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(imageVO, response.getData());
        verify(imageService, times(1)).getImageById(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    // ==================== 图片更新测试 ====================

    /**
     * TC-IMG-CONTROLLER-006: 更新图片信息-成功
     */
    @Test
    void testUpdateImage_Success() {
        ImageUpdateDTO updateDTO = new ImageUpdateDTO();
        updateDTO.setDescription("更新后的描述");

        ImageVO imageVO = createTestImageVO();
        imageVO.setDescription("更新后的描述");

        when(imageService.updateImage(eq(TEST_IMAGE_ID), any(ImageUpdateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.updateImage(TEST_IMAGE_ID, updateDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("更新成功", response.getMessage());
        verify(imageService, times(1)).updateImage(eq(TEST_IMAGE_ID), any(ImageUpdateDTO.class), eq(TEST_USER_ID));
    }

    // ==================== 图片删除测试 ====================

    /**
     * TC-IMG-CONTROLLER-007: 删除图片-成功
     */
    @Test
    void testDeleteImage_Success() {
        doNothing().when(imageService).deleteImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));

        ApiResponse<Void> response = imageController.deleteImage(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("删除成功", response.getMessage());
        verify(imageService, times(1)).deleteImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-008: 批量删除图片-成功
     */
    @Test
    void testBatchDeleteImages_Success() {
        List<String> ids = Arrays.asList(TEST_IMAGE_ID, "img_002");
        when(imageService.batchDeleteImages(eq(ids), eq(TEST_USER_ID)))
                .thenReturn(2);

        ApiResponse<Integer> response = imageController.batchDeleteImages(ids);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertTrue(response.getMessage().contains("批量删除成功"));
        assertEquals(2, response.getData());
        verify(imageService, times(1)).batchDeleteImages(eq(ids), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-009: 恢复图片-成功
     */
    @Test
    void testRestoreImage_Success() {
        ImageVO imageVO = createTestImageVO();
        when(imageService.restoreImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.restoreImage(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("恢复成功", response.getMessage());
        verify(imageService, times(1)).restoreImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-010: 归档图片-成功
     */
    @Test
    void testArchiveImage_Success() {
        ImageVO imageVO = createTestImageVO();
        when(imageService.archiveImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.archiveImage(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("归档成功", response.getMessage());
        verify(imageService, times(1)).archiveImage(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    // ==================== 图片统计测试 ====================

    /**
     * TC-IMG-CONTROLLER-011: 查询图片统计信息-成功
     */
    @Test
    void testGetStatistics_Success() {
        ImageStatisticsVO statistics = new ImageStatisticsVO();
        statistics.setTotalCount(10L);
        statistics.setTotalSize(1024000L);

        when(imageService.getStatistics(eq(TEST_USER_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(statistics);

        ApiResponse<ImageStatisticsVO> response = imageController.getStatistics("2025-01-01 00:00:00", "2025-01-31 23:59:59");

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(10L, response.getData().getTotalCount());
        verify(imageService, times(1)).getStatistics(eq(TEST_USER_ID), any(), any());
    }

    // ==================== 图片分享测试 ====================

    /**
     * TC-IMG-CONTROLLER-012: 创建分享链接-成功
     */
    @Test
    void testCreateShare_Success() {
        ImageShareDTO shareDTO = new ImageShareDTO();
        shareDTO.setExpiresAt(LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        ImageShareVO shareVO = new ImageShareVO();
        shareVO.setShareToken("token_123");
        shareVO.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(imageShareService.createShare(eq(TEST_IMAGE_ID), any(ImageShareDTO.class), eq(TEST_USER_ID)))
                .thenReturn(shareVO);

        ApiResponse<ImageShareVO> response = imageController.createShare(TEST_IMAGE_ID, shareDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("创建分享链接成功", response.getMessage());
        verify(imageShareService, times(1)).createShare(eq(TEST_IMAGE_ID), any(ImageShareDTO.class), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-013: 取消分享-成功
     */
    @Test
    void testCancelShare_Success() {
        doNothing().when(imageShareService).cancelShare(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));

        ApiResponse<Void> response = imageController.cancelShare(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("取消分享成功", response.getMessage());
        verify(imageShareService, times(1)).cancelShare(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-014: 根据分享令牌获取图片信息-成功
     */
    @Test
    void testGetShareInfo_Success() {
        String shareToken = "token_123";
        ImageShareVO shareVO = new ImageShareVO();
        shareVO.setShareToken(shareToken);

        when(imageShareService.getShareInfo(eq(shareToken)))
                .thenReturn(shareVO);

        ApiResponse<ImageShareVO> response = imageController.getShareInfo(shareToken);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(shareVO, response.getData());
        verify(imageShareService, times(1)).getShareInfo(eq(shareToken));
    }

    // ==================== 图片访问测试 ====================

    /**
     * TC-IMG-CONTROLLER-015: 通过ID访问图片-成功
     */
    @Test
    void testAccessImageById_Success() {
        ImageVO imageVO = createTestImageVO();
        when(imageAccessService.accessImageById(eq(TEST_IMAGE_ID), eq(TEST_USER_ID)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.accessImageById(TEST_IMAGE_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(imageVO, response.getData());
        verify(imageAccessService, times(1)).accessImageById(eq(TEST_IMAGE_ID), eq(TEST_USER_ID));
    }

    /**
     * TC-IMG-CONTROLLER-016: 通过分享令牌访问图片-成功
     */
    @Test
    void testAccessImageByShareToken_Success() {
        String shareToken = "token_123";
        ImageVO imageVO = createTestImageVO();
        when(imageAccessService.accessImageByShareToken(eq(shareToken)))
                .thenReturn(imageVO);

        ApiResponse<ImageVO> response = imageController.accessImageByShareToken(shareToken);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals(imageVO, response.getData());
        verify(imageAccessService, times(1)).accessImageByShareToken(eq(shareToken));
    }

    /**
     * TC-IMG-CONTROLLER-017: 读取图片文件内容-成功
     */
    @Test
    void testReadImageFile_Success() {
        String path = "avatar/2025-01/test.jpg";
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        when(imageAccessService.readImageFile(eq(path)))
                .thenReturn(fileBytes);

        ResponseEntity<byte[]> response = imageController.readImageFile(path);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(fileBytes, response.getBody());
        verify(imageAccessService, times(1)).readImageFile(eq(path));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的ImageVO对象
     */
    private ImageVO createTestImageVO() {
        ImageVO imageVO = new ImageVO();
        imageVO.setId(TEST_IMAGE_ID);
        imageVO.setOriginalFilename("test.jpg");
        imageVO.setFileUrl("http://localhost/uploads/test.jpg");
        imageVO.setFileSize(1024L);
        imageVO.setWidth(100);
        imageVO.setHeight(100);
        imageVO.setCategory("avatar");
        imageVO.setStatus("ACTIVE");
        return imageVO;
    }
}

