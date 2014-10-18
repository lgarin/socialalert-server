package com.bravson.socialalert.common.facade;

import java.io.IOException;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

@JsonRpcService(value="controlFacade", useNamedParams=true)
public interface ControlFacade {

	List<String> listValidValues(@JsonRpcParam("valueSet") @NotEmpty String valueSet) throws IOException;
}