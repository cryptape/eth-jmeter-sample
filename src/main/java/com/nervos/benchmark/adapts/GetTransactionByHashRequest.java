package com.nervos.benchmark.adapts;

import com.nervos.benchmark.model.TxMsg;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.util.List;


public class GetTransactionByHashRequest extends Web3BasicRequest{

    List<TxMsg> txMsgs;
    Integer currentSendIdx = 0;
    TxMsg currentTxMsg;


    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.SIZE, "1");
        return arguments;
    }

    @Override
    public void setupOtherData(JavaSamplerContext context) {
        txMsgs = SingletonService.getSingletonTxList(this.web3j,context.getIntParameter(Constant.SIZE));

    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        currentSendIdx++;
        currentTxMsg = txMsgs.get(currentSendIdx / txMsgs.size());
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        Transaction tx;
        try {
            tx = this.web3j.ethGetTransactionByHash(currentTxMsg.getTxHash()).send().getTransaction().get();
            if (tx.getBlockNumber().compareTo(currentTxMsg.getBlockNum()) == 0
                    && tx.getHash().equals(tx.getHash())
                    && tx.getTransactionIndex().compareTo(currentTxMsg.getIdx()) == 0
                    && tx.getBlockHash().equals(currentTxMsg.getBlkHash())
            ) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
