package org.colxj.core;


import org.darkcoinj.DarkSendSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Hash Engineering on 2/20/2016.
 */
public class SporkManager {
    private static final Logger log = LoggerFactory.getLogger(SporkManager.class);
    private String strSporkKey;

    AbstractBlockChain blockChain;
    HashMap<Sha256Hash, SporkMessage> mapSporks;
    HashMap<Integer, SporkMessage> mapSporksActive;

    SporkManager()
    {
        mapSporks = new HashMap<Sha256Hash, SporkMessage>();
        mapSporksActive = new HashMap<Integer, SporkMessage>();
    }

    void setSporkKey(String key)
    {
        this.strSporkKey = key;
    }

    void setBlockChain(AbstractBlockChain blockChain)
    {
        this.blockChain = blockChain;
    }

    void processSpork(Peer from, SporkMessage spork) {
        int height = -1;
        if (blockChain != null)
            height = blockChain.getBestChainHeight();

        Sha256Hash hash = spork.getHash();
        if (mapSporksActive.containsKey(spork.nSporkID)) {
            if (mapSporksActive.get(spork.nSporkID).nTimeSigned >= spork.nTimeSigned) {
                log.info("spork - seen {} block {}", hash.toString(), height);
                return;
            }
        }

        if (!checkSignature(spork)) {
            log.error("{}. Invalid spork signature {}.", this, spork.nSporkID);
            return;
        }

        mapSporks.put(hash, spork);
        mapSporksActive.put(spork.nSporkID, spork);
        log.info("spork - got updated spork {} block {}", hash.toString(), height);
    }

    public static final int SPORK_2_SWIFTTX = 10001;
    public static final int SPORK_3_SWIFTTX_BLOCK_FILTERING = 10002;
    public static final int SPORK_5_MAX_VALUE = 10004;
    public static final int SPORK_7_MASTERNODE_SCANNING = 10006;
    public static final int SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT = 10007;
    public static final int SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT = 10008;
    public static final int SPORK_10_MASTERNODE_PAY_UPDATED_NODES = 10009;
    public static final int SPORK_13_ENABLE_SUPERBLOCKS = 10012;
    public static final int SPORK_14_NEW_PROTOCOL_ENFORCEMENT = 10013;
    public static final int SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2 = 10014;
    public static final int SPORK_17_FEE_PAYMENT_ENFORCEMENT = 10016;
    public static final int SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT = 10017;
    public static final int SPORK_19_MAX_REORGANIZATION_DEPTH = 10018;
    public static final int SPORK_20_ZEROCOIN_MAINTENANCE_MODE = 10019;
    public static final int SPORK_21_ENFORCE_MIN_TX_FEE = 10020;
    public static final int SPORK_22_TX_FEE_VALUE = 10021;
    public static final int SPORK_23_LIMIT_BLOCK_TX = 10022;
    public static final int SPORK_24_BLOCK_TX_VALUE = 10023;
    public static final int SPORK_25_RESERVED = 10024;

    public static final long SPORK_2_SWIFTTX_DEFAULT = 978307200;                          //2001-1-1
    public static final long SPORK_3_SWIFTTX_BLOCK_FILTERING_DEFAULT = 1424217600;         //2015-2-18
    public static final long SPORK_5_MAX_VALUE_DEFAULT = 1000000;                          //1'000'000 COLX
    public static final long SPORK_7_MASTERNODE_SCANNING_DEFAULT = 978307200;              //2001-1-1
    public static final long SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT = 4070908800L; //OFF
    public static final long SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT_DEFAULT = 4070908800L;  //OFF
    public static final long SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT = 4070908800L;  //OFF
    public static final long SPORK_13_ENABLE_SUPERBLOCKS_DEFAULT = 4070908800L;            //OFF
    public static final long SPORK_14_NEW_PROTOCOL_ENFORCEMENT_DEFAULT = 4070908800L;      //OFF
    public static final long SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2_DEFAULT = 4070908800L;    //OFF
    public static final long SPORK_17_FEE_PAYMENT_ENFORCEMENT_DEFAULT = 4070908800L;       //OFF
    public static final long SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT_DEFAULT = 4070908800L;   //OFF
    public static final long SPORK_19_MAX_REORGANIZATION_DEPTH_DEFAULT = 1000;             //1000 blocks
    public static final long SPORK_20_ZEROCOIN_MAINTENANCE_MODE_DEFAULT = 4070908800L;     //OFF
    public static final long SPORK_21_ENFORCE_MIN_TX_FEE_DEFAULT = 4070908800L;            //OFF
    public static final long SPORK_22_TX_FEE_VALUE_DEFAULT = 10;                           //10 COLX
    public static final long SPORK_23_LIMIT_BLOCK_TX_DEFAULT = 4070908800L;                //OFF
    public static final long SPORK_24_BLOCK_TX_VALUE_DEFAULT = 1;                          //1 tx per block
    public static final long SPORK_25_RESERVED_DEFAULT = 4070908800L;                      //OFF

    public boolean isSporkActive(int nSporkID)
    {
        long r = getSporkValue(nSporkID);
        if (r < 0)
            return  false;
        else
            return r < Utils.currentTimeSeconds();
    }

    // grab the value of the spork on the network, or the default
    public long getSporkValue(int nSporkID)
    {
        long r = -1;

        if(mapSporksActive.containsKey(nSporkID)){
            return mapSporksActive.get(nSporkID).nValue;
        } else {
            switch (nSporkID) {
                case SPORK_2_SWIFTTX: return SPORK_2_SWIFTTX_DEFAULT;
                case SPORK_3_SWIFTTX_BLOCK_FILTERING: return SPORK_3_SWIFTTX_BLOCK_FILTERING_DEFAULT;
                case SPORK_5_MAX_VALUE: return SPORK_5_MAX_VALUE_DEFAULT;
                case SPORK_7_MASTERNODE_SCANNING: return SPORK_7_MASTERNODE_SCANNING_DEFAULT;
                case SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT: return SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT;
                case SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT: return SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT_DEFAULT;
                case SPORK_10_MASTERNODE_PAY_UPDATED_NODES: return SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT;
                case SPORK_13_ENABLE_SUPERBLOCKS: return SPORK_13_ENABLE_SUPERBLOCKS_DEFAULT;
                case SPORK_14_NEW_PROTOCOL_ENFORCEMENT: return SPORK_14_NEW_PROTOCOL_ENFORCEMENT_DEFAULT;
                case SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2: return SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2_DEFAULT;
                case SPORK_17_FEE_PAYMENT_ENFORCEMENT: return SPORK_17_FEE_PAYMENT_ENFORCEMENT_DEFAULT;
                case SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT: return SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT_DEFAULT;
                case SPORK_19_MAX_REORGANIZATION_DEPTH: return SPORK_19_MAX_REORGANIZATION_DEPTH_DEFAULT;
                case SPORK_20_ZEROCOIN_MAINTENANCE_MODE: return SPORK_20_ZEROCOIN_MAINTENANCE_MODE_DEFAULT;
                case SPORK_21_ENFORCE_MIN_TX_FEE: return SPORK_21_ENFORCE_MIN_TX_FEE_DEFAULT;
                case SPORK_22_TX_FEE_VALUE: return SPORK_22_TX_FEE_VALUE_DEFAULT;
                case SPORK_23_LIMIT_BLOCK_TX: return SPORK_23_LIMIT_BLOCK_TX_DEFAULT;
                case SPORK_24_BLOCK_TX_VALUE: return SPORK_24_BLOCK_TX_VALUE_DEFAULT;
                case SPORK_25_RESERVED: return SPORK_25_RESERVED_DEFAULT;
                default:
                    log.error("{}. Unknown spork {}.", this, nSporkID);
                    return -1;
            }
        }
    }

    public ArrayList<String> getSporkList() {
        ArrayList<String> sporkList = new ArrayList<String>();
        sporkList.add(String.format("SPORK_2_SWIFTTX = %d", getSporkValue(SPORK_2_SWIFTTX)));
        sporkList.add(String.format("SPORK_3_SWIFTTX_BLOCK_FILTERING = %d", getSporkValue(SPORK_3_SWIFTTX_BLOCK_FILTERING)));
        sporkList.add(String.format("SPORK_5_MAX_VALUE = %d", getSporkValue(SPORK_5_MAX_VALUE)));
        sporkList.add(String.format("SPORK_7_MASTERNODE_SCANNING = %d", getSporkValue(SPORK_7_MASTERNODE_SCANNING)));
        sporkList.add(String.format("SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT = %d", getSporkValue(SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT)));
        sporkList.add(String.format("SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT = %d", getSporkValue(SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT)));
        sporkList.add(String.format("SPORK_10_MASTERNODE_PAY_UPDATED_NODES = %d", getSporkValue(SPORK_10_MASTERNODE_PAY_UPDATED_NODES)));
        sporkList.add(String.format("SPORK_13_ENABLE_SUPERBLOCKS = %d", getSporkValue(SPORK_13_ENABLE_SUPERBLOCKS)));
        sporkList.add(String.format("SPORK_14_NEW_PROTOCOL_ENFORCEMENT = %d", getSporkValue(SPORK_14_NEW_PROTOCOL_ENFORCEMENT)));
        sporkList.add(String.format("SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2 = %d", getSporkValue(SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2)));
        sporkList.add(String.format("SPORK_17_FEE_PAYMENT_ENFORCEMENT = %d", getSporkValue(SPORK_17_FEE_PAYMENT_ENFORCEMENT)));
        sporkList.add(String.format("SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT = %d", getSporkValue(SPORK_18_DEVFUND_PAYMENT_ENFORCEMENT)));
        sporkList.add(String.format("SPORK_19_MAX_REORGANIZATION_DEPTH = %d", getSporkValue(SPORK_19_MAX_REORGANIZATION_DEPTH)));
        sporkList.add(String.format("SPORK_20_ZEROCOIN_MAINTENANCE_MODE = %d", getSporkValue(SPORK_20_ZEROCOIN_MAINTENANCE_MODE)));
        sporkList.add(String.format("SPORK_21_ENFORCE_MIN_TX_FEE = %d", getSporkValue(SPORK_21_ENFORCE_MIN_TX_FEE)));
        sporkList.add(String.format("SPORK_22_TX_FEE_VALUE = %d", getSporkValue(SPORK_22_TX_FEE_VALUE)));
        sporkList.add(String.format("SPORK_23_LIMIT_BLOCK_TX = %d", getSporkValue(SPORK_23_LIMIT_BLOCK_TX)));
        sporkList.add(String.format("SPORK_24_BLOCK_TX_VALUE = %d", getSporkValue(SPORK_24_BLOCK_TX_VALUE)));
        sporkList.add(String.format("SPORK_25_RESERVED = %d", getSporkValue(SPORK_25_RESERVED)));
        return sporkList;
    }

    private boolean checkSignature(SporkMessage spork)
    {
        String strMessage = "" + spork.nSporkID + spork.nValue + spork.nTimeSigned;
        PublicKey pubkey = new PublicKey(Utils.HEX.decode(strSporkKey));

        StringBuilder errorMessage = new StringBuilder();
        if(!DarkSendSigner.verifySporkMessage(pubkey, spork.sig, strMessage, errorMessage)){
            log.error(errorMessage.toString());
            return false;
        } else
            return true;
    }
}
