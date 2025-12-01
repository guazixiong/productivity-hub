package com.pbad.customer.controller;

import com.github.pagehelper.PageInfo;
import com.pbad.customer.domain.Customer;
import com.pbad.customer.domain.bo.CustomerBankBO;
import com.pbad.customer.domain.dto.CustomerDTO;
import com.pbad.customer.domain.po.CustomerPO;
import com.pbad.customer.domain.vo.CustomerBankInfoVO;
import com.pbad.customer.domain.vo.CustomerPageVO;
import com.pbad.customer.service.ICustomerService;
import common.convert.PageInfoConverter;
import common.core.domain.Response;
import common.util.SpringCopyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客商controller接口.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:46
 * @version: 1.0
 */
@Api(tags = "客商controller接口")
@RestController
@RequestMapping(path = "/business/customer/")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService iCustomerService;

    /**
     * 分页查询.
     *
     * @param customerDTO 客商银行信息传输类
     * @return 分页信息
     */
    @ApiOperation(value = "分页列表")
    @GetMapping("/page")
    public Response<PageInfo<CustomerPageVO>> page(@ModelAttribute CustomerDTO customerDTO) {
        Customer customer = SpringCopyUtil.convert(customerDTO, Customer.class);
        PageInfo<CustomerPO> pageList = iCustomerService.page(customer);
        PageInfoConverter<CustomerPO, CustomerPageVO> converter = new PageInfoConverter<>();
        PageInfo<CustomerPageVO> pageInfo = converter.convertAndCheckEmpty(pageList, CustomerPageVO.class);
        return Response.ok(pageInfo);
    }

    /**
     * 新增客商.
     *
     * @param customerDTO 客商银行信息传输类
     * @return 响应结果
     */
    @ApiOperation(value = "新增客商")
    @PostMapping()
    public Response<Void> add(@Validated @RequestBody CustomerDTO customerDTO) {
        Customer customer = SpringCopyUtil.convert(customerDTO, Customer.class);
        iCustomerService.add(customer);
        return Response.ok();
    }

    /**
     * 修改客商.
     *
     * @param customerDTO 客商银行信息传输类
     * @return 响应结果
     */
    @ApiOperation(value = "修改客商")
    @PutMapping()
    public Response<Void> update(@RequestBody CustomerDTO customerDTO) {
        Customer customer = SpringCopyUtil.convert(customerDTO, Customer.class);
        iCustomerService.update(customer);
        return Response.ok();
    }

    /**
     * 客商详情.
     *
     * @param customerDTO 客商银行信息传输类
     * @return 客商银行信息VO类
     */
    @ApiOperation(value = "客商详情.", notes = "客商详情.", response = CustomerBankInfoVO.class)
    @GetMapping("/getInfo")
    public Response<CustomerBankInfoVO> getInfo(@ModelAttribute CustomerDTO customerDTO) {
        Customer customer = SpringCopyUtil.convert(customerDTO, Customer.class);
        CustomerBankBO serviceInfo = iCustomerService.getInfo(customer);
        CustomerBankInfoVO bankInfoVO = SpringCopyUtil.convert(serviceInfo, CustomerBankInfoVO.class);
        return Response.ok(bankInfoVO);
    }

    /**
     * 删除客商.
     *
     * @param customerId 客商id
     * @return 响应结果
     */
    @ApiOperation(value = "删除客商")
    @DeleteMapping("/{customerId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "customerId", value = "客商编号", required = true)})
    public Response<Void> remove(@PathVariable("customerId") String customerId) {
        iCustomerService.deleteValidById(customerId, true);
        return Response.ok();
    }

}
