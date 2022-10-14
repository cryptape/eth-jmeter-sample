package com.nervos.benckmark.adapts;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;
import java.util.List;

public class GetBalanceRequest extends Web3BasicRequest {

    private List<String> addressList;
    private Integer currentRunSize = 0;


    private String currentQueryAddress;

    private DefaultBlockParameterName defaultBlockParameterName;

    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.ACCOUNT_SIZE, "1");
        arguments.addArgument(Constant.DefaultBlockParameterName, DefaultBlockParameterName.PENDING.getValue());
        return arguments;
    }

    @Override
    public void setupOtherData(JavaSamplerContext context) {
        Integer accountSize = context.getIntParameter(Constant.ACCOUNT_SIZE);
        addressList = SingletonService.getSingletonAddressList(this.web3j, accountSize);
        this.defaultBlockParameterName = DefaultBlockParameterName.valueOf(context.getParameter(Constant.DefaultBlockParameterName));
    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        currentRunSize++;
        this.currentQueryAddress = addressList.get(currentRunSize / addressList.size());
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        try {
            BigInteger balance = this.web3j.ethGetBalance(currentQueryAddress, this.defaultBlockParameterName).send().getBalance();
            if (balance.compareTo(new BigInteger("0")) >= 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
