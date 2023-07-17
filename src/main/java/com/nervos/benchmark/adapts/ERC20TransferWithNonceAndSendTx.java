package com.nervos.benchmark.adapts;

import com.nervos.benchmark.contracts.BEP20;
import com.nervos.benchmark.model.Account;
import com.nervos.benchmark.util.TransactionUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ERC20TransferWithNonceAndSendTx extends Web3BasicRequest {

    public BEP20 bep20;
    private List<Account> accountList;
    private static AtomicInteger curSendIdx = new AtomicInteger(0);
    private String data = "";
    private Credentials currentSendCredentials;
    private BigInteger chainId;
    private BigInteger gasPrice;
    private BigInteger gasLimit;


    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.Mnemonic, Constant.DEFAULT_MNEMONIC);
        arguments.addArgument(Constant.SIZE,"10");
        arguments.addArgument(Constant.ERC20_Address, "");
        arguments.addArgument(Constant.GasLimit, "1000000");
        arguments.addArgument(Constant.GasPrice, "10000");

        return arguments;
    }

    @Override
    public void setupOtherData(JavaSamplerContext context) {
        String privates = context.getParameter(Constant.Mnemonic);
        int size = context.getIntParameter(Constant.SIZE);
        String contractAddress = context.getParameter(Constant.ERC20_Address);
        this.chainId = SingletonService.getChainId(this.web3j);

        this.accountList = SingletonService.getSingletonAccountList(privates,size);
        //deploy contract
        this.bep20 = SingletonService.getSingletonBEP20(this.accountList.get(0).getCredentials(), this.web3j, contractAddress, this.chainId.intValue());
        this.gasLimit = new BigInteger(context.getParameter(Constant.GasLimit));
        this.gasPrice = new BigInteger(context.getParameter(Constant.GasPrice));
    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        this.data = this.bep20.transfer(this.bep20.getContractAddress(), new BigInteger("0")).encodeFunctionCall();
        int currentIdx = curSendIdx.getAndAdd(1) % this.accountList.size();
        System.out.println("currentIdx:" + currentIdx);
        this.currentSendCredentials = this.accountList.get(currentIdx).getCredentials();
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        return sendTx(this.web3j, this.currentSendCredentials, this.bep20.getContractAddress(), new BigInteger("0"), this.data);
    }


    private boolean sendTx(Web3j web3j, Credentials fromCredentials, String contractAddress, BigInteger bigInteger, String payload) {
        try {
            String hexStr = TransactionUtil.signTx(this.web3j, fromCredentials, gasPrice, gasLimit, contractAddress, bigInteger, payload);
            String txHash = web3j.ethSendRawTransaction(hexStr).send().getTransactionHash();
            System.out.println("txHash:" + txHash);
            if (txHash.length() > 10) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
