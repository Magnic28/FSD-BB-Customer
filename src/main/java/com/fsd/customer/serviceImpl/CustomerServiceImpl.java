package com.fsd.customer.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsd.customer.bean.*;
import com.fsd.customer.client.VendorClient;
import com.fsd.customer.dao.CustomerDao;
import com.fsd.customer.entity.UserEntity;
import com.fsd.customer.exception.CustomerException;
import com.fsd.customer.helper.CustomerHashUtils;
import com.fsd.customer.helper.MapperUtil;
import com.fsd.customer.service.CustomerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerDao customerDao;

    @Autowired
    VendorClient vendorClient;


    @Override
    @Transactional
    public SignUpResponse userSignUp(UserSignUpRequest userSignUpRequest) {
        String userId= userSignUpRequest.getRole().substring(0,2)+CustomerHashUtils.getId();
        UserEntity userEntity=new UserEntity(userId,userSignUpRequest.getUserName(),userSignUpRequest.getPassword(),userSignUpRequest.getEmail(),userSignUpRequest.getMobile(),userSignUpRequest.getRole());
        if(null!= customerDao.saveUser(userEntity)) {
            if(userSignUpRequest.getRole().equalsIgnoreCase("VENDOR")){
                RegisterVendorRequestBean registerVendorRequestBean = MapperUtil.getRegisterVendorRequestBean(userSignUpRequest, userId);
                ResponseEntity<String> resp=vendorClient.registerVendor(registerVendorRequestBean);
            }
            return new SignUpResponse(userId, "Registered Successfully");
        }else {
            throw new CustomerException(new ErrorBean("CU_001","Not Registered"), HttpStatus.INTERNAL_SERVER_ERROR);
        }


}

    @Override
public String userLogin(UserLoginRequest userLoginRequest) {
    UserEntity userEntity=customerDao.getUserByMobile(userLoginRequest.getMobile());
    if(userEntity.getRole().equals(userLoginRequest.getRole())&&
            userEntity.getPassword().equals(userLoginRequest.getPassword()))
        return "Login Successful";
    else
        return "Login Failed";
}

@Override
public UserEntity getUserDetails(String userId) {
    return customerDao.getUserbyUserId(userId);
}
}