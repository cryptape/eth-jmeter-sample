package com.nervos.benchmark.adapts;

import com.nervos.benchmark.model.Account;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GetBalance extends Web3BasicRequest {

    private List<Account> accountList;
    private static AtomicInteger curBalanceCheckIdx = new AtomicInteger(0);
    private int currentIdx;
    private Account currentAccount;

    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.Mnemonic, Constant.DEFAULT_MNEMONIC);
        arguments.addArgument(Constant.SIZE, "100");
        return arguments;
    }

    @Override
    public void setupOtherData(JavaSamplerContext context) {
        String mnstr = context.getParameter(Constant.Mnemonic);
        int size = context.getIntParameter(Constant.SIZE);
        this.accountList = SingletonService.getSingletonAccountList(mnstr, size);
    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        this.currentIdx = curBalanceCheckIdx.getAndAdd(1) % this.accountList.size();
        this.currentAccount = this.accountList.get(currentIdx);
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        return checkBalance(this.web3j, this.currentIdx, this.currentAccount);
    }

    private boolean checkBalance(Web3j web3j, int index, Account account) {
        try {
            Credentials credentials = account.getCredentials();
            String address = credentials.getAddress();
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            BigInteger balanceInWei = ethGetBalance.getBalance();
            System.out.println("Account " + index + " : " + address + " has " + Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER) + " Ether");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }
}
