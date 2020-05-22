package org.colxj.core;


import org.darkcoinj.DarkSendSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by Hash Engineering on 2/20/2016.
 */
public class SporkManager {
    private static final Logger log = LoggerFactory.getLogger(SporkManager.class);

    Context context;
    AbstractBlockChain blockChain;
    HashMap<Sha256Hash, SporkMessage> mapSporks;
    HashMap<Integer, SporkMessage> mapSporksActive;

    SporkManager(Context context)
    {
        this.context = context;
        mapSporks = new HashMap<Sha256Hash, SporkMessage>();
        mapSporksActive = new HashMap<Integer, SporkMessage>();
    }

    void setBlockChain(AbstractBlockChain blockChain)
    {
        this.blockChain = blockChain;
    }

    void processSpork(Peer from, SporkMessage spork) {
        if (blockChain == null) {
            log.error("{}. blockChain is null", this);
            return;
        }

        Sha256Hash hash = spork.getHash();
        if (mapSporksActive.containsKey(spork.nSporkID)) {
            if (mapSporksActive.get(spork.nSporkID).nTimeSigned >= spork.nTimeSigned) {
                log.info("spork - seen "+hash.toString()+" block " + blockChain.getBestChainHeight());
                return;
            }
        }

        //log.info("spork - new "+hash.toString()+" ID "+spork.nSporkID+" Time "+spork.nTimeSigned+" Value " + spork.nValue + " Signature " + Utils.HEX.encode(spork.sig.bytes));
        //log.info("spork hash bytes: " + Utils.HEX.encode(spork.getBytes()));

        if (!checkSignature(spork)) {
            log.error("{}. Invalid spork signature {}.", this, spork.nSporkID);
            return;
        }

        mapSporks.put(hash, spork);
        mapSporksActive.put(spork.nSporkID, spork);
        log.info("spork - got updated spork "+hash.toString()+" block " +blockChain.getBestChainHeight());
    }

    public static final int SPORK_2_INSTANTSEND_ENABLED                            = 10001;
    public static final int SPORK_3_INSTANTSEND_BLOCK_FILTERING                    = 10002;
    public static final int SPORK_5_INSTANTSEND_MAX_VALUE                          = 10004;
    public static final int SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT                 = 10007;
    public static final int SPORK_9_SUPERBLOCKS_ENABLED                            = 10008;
    public static final int SPORK_10_MASTERNODE_PAY_UPDATED_NODES                  = 10009;
    public static final int SPORK_12_RECONSIDER_BLOCKS                             = 10011;
    public static final int SPORK_13_OLD_SUPERBLOCK_FLAG                           = 10012;
    public static final int SPORK_14_REQUIRE_SENTINEL_FLAG                         = 10013;

    public static final long SPORK_2_INSTANTSEND_ENABLED_DEFAULT                = 0;            // ON
    public static final long SPORK_3_INSTANTSEND_BLOCK_FILTERING_DEFAULT        = 0;            // ON
    public static final long SPORK_5_INSTANTSEND_MAX_VALUE_DEFAULT              = 1000;         // 1000 DASH
    public static final long SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT     = 4070908800L;// OFF
    public static final long SPORK_9_SUPERBLOCKS_ENABLED_DEFAULT                = 4070908800L;// OFF
    public static final long SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT      = 4070908800L;// OFF
    public static final long SPORK_12_RECONSIDER_BLOCKS_DEFAULT                 = 0;            // 0 BLOCKS
    public static final long SPORK_13_OLD_SUPERBLOCK_FLAG_DEFAULT               = 4070908800L;// OFF
    public static final long SPORK_14_REQUIRE_SENTINEL_FLAG_DEFAULT             = 4070908800L;// OFF

    // grab the spork, otherwise say it's off
    public boolean isSporkActive(int nSporkID)
    {
        long r = -1;

        if(mapSporksActive.containsKey(nSporkID)){
            r = mapSporksActive.get(nSporkID).nValue;
        } else {
            switch (nSporkID) {
                case SPORK_2_INSTANTSEND_ENABLED:               r = SPORK_2_INSTANTSEND_ENABLED_DEFAULT; break;
                case SPORK_3_INSTANTSEND_BLOCK_FILTERING:       r = SPORK_3_INSTANTSEND_BLOCK_FILTERING_DEFAULT; break;
                case SPORK_5_INSTANTSEND_MAX_VALUE:             r = SPORK_5_INSTANTSEND_MAX_VALUE_DEFAULT; break;
                case SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT:    r = SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT; break;
                case SPORK_9_SUPERBLOCKS_ENABLED:               r = SPORK_9_SUPERBLOCKS_ENABLED_DEFAULT; break;
                case SPORK_10_MASTERNODE_PAY_UPDATED_NODES:     r = SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT; break;
                case SPORK_12_RECONSIDER_BLOCKS:                r = SPORK_12_RECONSIDER_BLOCKS_DEFAULT; break;
                case SPORK_13_OLD_SUPERBLOCK_FLAG:              r = SPORK_13_OLD_SUPERBLOCK_FLAG_DEFAULT; break;
                case SPORK_14_REQUIRE_SENTINEL_FLAG:            r = SPORK_14_REQUIRE_SENTINEL_FLAG_DEFAULT; break;
                default:
                    log.info("spork", "CSporkManager::IsSporkActive -- Unknown Spork ID" + nSporkID);
                    r = 4070908800L; // 2099-1-1 i.e. off by default
                    break;
            }
        }

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
                case SPORK_2_INSTANTSEND_ENABLED:               return SPORK_2_INSTANTSEND_ENABLED_DEFAULT;
                case SPORK_3_INSTANTSEND_BLOCK_FILTERING:       return SPORK_3_INSTANTSEND_BLOCK_FILTERING_DEFAULT;
                case SPORK_5_INSTANTSEND_MAX_VALUE:             return SPORK_5_INSTANTSEND_MAX_VALUE_DEFAULT;
                case SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT:    return SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT;
                case SPORK_9_SUPERBLOCKS_ENABLED:               return SPORK_9_SUPERBLOCKS_ENABLED_DEFAULT;
                case SPORK_10_MASTERNODE_PAY_UPDATED_NODES:     return SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT;
                case SPORK_12_RECONSIDER_BLOCKS:                return SPORK_12_RECONSIDER_BLOCKS_DEFAULT;
                case SPORK_13_OLD_SUPERBLOCK_FLAG:              return SPORK_13_OLD_SUPERBLOCK_FLAG_DEFAULT;
                case SPORK_14_REQUIRE_SENTINEL_FLAG:            return SPORK_14_REQUIRE_SENTINEL_FLAG_DEFAULT;
                default:
                    log.info("spork", "CSporkManager::GetSporkValue -- Unknown Spork ID "+ nSporkID);
                    return -1;
            }
        }
    }

    private boolean checkSignature(SporkMessage spork)
    {
        String strMessage = "" + spork.nSporkID + spork.nValue + spork.nTimeSigned;
        PublicKey pubkey = new PublicKey(Utils.HEX.decode(context.getParams().getSporkKey()));

        StringBuilder errorMessage = new StringBuilder();
        if(!DarkSendSigner.verifySporkMessage(pubkey, spork.sig, strMessage, errorMessage)){
            log.error(errorMessage.toString());
            return false;
        } else
            return true;
    }
}
