package com.ry.cbms.decision.server.service.impl;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.dao.AccessGoldCheckDao;
import com.ry.cbms.decision.server.dao.FileInfoDao;
import com.ry.cbms.decision.server.model.AccessGoldCheck;
import com.ry.cbms.decision.server.model.FileInfo;
import com.ry.cbms.decision.server.service.FileService;
import com.ry.cbms.decision.server.service.PaymentChannelService;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.utils.ExcelUtil;
import com.ry.cbms.decision.server.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @Author maoYang
 * @Date 2019/5/21 14:07
 * @Description 支付通道相关服务
 */
@Service
@Slf4j
public class PaymentChannelServiceImpl implements PaymentChannelService {
    @Autowired
    private AccessGoldCheckDao accessGoldCheckDao;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileInfoDao fileInfoDao;

    @Override
    public Integer getChannelCashCount(Map<String, Object> params) {
        Integer count = 0;
        try {
            if (Constants.rCheckKind.equals (params.get ("checkKind").toString ())) {//入金列表
                count = accessGoldCheckDao.getChannelCashInCount (params);
            } else {
                count = accessGoldCheckDao.getChannelCashOutCount (params);
            }
        } catch (Exception e) {
            log.error ("入金数量查询异常{}", e);
            return count;
        }
        return count;
    }

    /**
     * 获取入金对账单
     *
     * @param params
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List getChannelCashList(Map<String, Object> params, Integer offset, Integer limit) {
        List<Map> channelList;
        List<AccessGoldCheck> checks = new ArrayList<> ();
        List<Map> channelCheckList = new ArrayList<> ();
        String checkKind;
        StringBuffer ids = new StringBuffer ();//入金记录的入金流水号集合
        try {
            if (Constants.rCheckKind.equals (params.get ("checkKind").toString ())) {//入金列表
                channelList = accessGoldCheckDao.getChannelCashInList (params, offset, limit);
            } else {
                channelList = accessGoldCheckDao.getChannelCashOutList (params, offset, limit); //出金
            }
            int listLen = channelList.size ();
            for (int i = 0; i < listLen; i++) {
                String id = channelList.get (i).get ("id") + ".0";
                if (Constants.rCheckKind.equals (params.get ("checkKind"))) {//入金列表
                    checkKind=Constants.rCheckKind+".0";
                    AccessGoldCheck accessGoldCheck = accessGoldCheckDao.selectChecksBySerialNum (checkKind, id);//入金对账单
                    checks.add (accessGoldCheck);
                } else {
                    checkKind=Constants.cCheckKind+".0";
                    AccessGoldCheck accessGoldCheck = accessGoldCheckDao.selectChecksBySerialNum (checkKind, id);//出金对账单
                    checks.add (accessGoldCheck);

                }
            }

            int len = channelList.size ();
            for (int i = 0; i < len; i++) {
                List channelCheckItems = new ArrayList<> ();
                channelCheckItems.add (channelList.get (i));
                if (checks.size () > i) {
                    channelCheckItems.add (checks.get (i));
                }
                Map itemMap = new HashMap ();
                itemMap.put (i, channelCheckItems);
                channelCheckList.add (itemMap);
            }
        } catch (Exception e) {
            log.error ("入金查询异常{}", e);
            return channelCheckList;
        }
        return channelCheckList;
    }

    /**
     * 对账操作
     *
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> checkBill(String startTime, String endTime, String checkKind) {
        List<Map> channelCashList;
        List<Map> removeChannelCashList=new ArrayList<> ();
        Integer checkOkNum = 0;
        Integer checkNotOkNum = 0;
        Map<String, Object> resultMap = new HashMap<> ();
        List<AccessGoldCheck> accessGoldChecks = new ArrayList<> ();//对账后的集合
        List<Map> channelChecks = new ArrayList<> ();//对账后的集合
        if (Constants.rCheckKind.equals (checkKind)) { //入金
            channelCashList = accessGoldCheckDao.getChannelCashIns (startTime, endTime);
        } else {
            channelCashList = accessGoldCheckDao.getChannelCashOuts (startTime, endTime);
        }
        int cashListLen = channelCashList.size ();
        List<AccessGoldCheck> checkBills = new ArrayList<> ();
        for (int j = 0; j < cashListLen; j++) {
            String id = channelCashList.get (j).get ("id") + ".0";
            checkKind = Constants.cCheckKind + ".0";
            AccessGoldCheck accessGoldCheck = accessGoldCheckDao.selectChecksBySerialNum (checkKind, id);//对账单
            if (null != accessGoldCheck) {
                removeChannelCashList.add (channelCashList.get (j));
                checkBills.add (accessGoldCheck);//对账单集合
            }
        }
        channelCashList.removeAll (removeChannelCashList);
        channelCashList.addAll (0,removeChannelCashList);

        for (int i = 0; i < cashListLen; i++) {
            Map cashInMap = channelCashList.get (i);
            AccessGoldCheck accessGoldCheck;
            if (checkBills.size () > i) {
                accessGoldCheck = checkBills.get (i);
                String actArr = accessGoldCheck.getActualArrival ().toString ();//财务实际到账
                Object moneyRmb = cashInMap.get ("actAmount");//每笔入金金额
                if (null == moneyRmb) {
                    moneyRmb = 0;
                }
                Double moneyDouble = Double.valueOf (moneyRmb.toString ());
                Double actArrDouble = Double.valueOf (actArr);
                if ( moneyDouble.doubleValue ()==actArrDouble.doubleValue ()) {  // 对账判断(出入金金额)
                  //  accessGoldCheck.setCheckResult (Constants.check_Bill_SUCC);
                    cashInMap.put ("checkResult", Constants.check_Bill_SUCC);
                    checkOkNum++;
                } else if (moneyDouble > actArrDouble) {
                   // accessGoldCheck.setCheckResult (Constants.check_Bill_Cash_Out_More);
                    cashInMap.put ("checkResult", Constants.check_Bill_Cash_Out_More);
                    checkNotOkNum++;
                } else if (moneyDouble < actArrDouble) {
                   // accessGoldCheck.setCheckResult (Constants.check_Bill_Cash_Out_Channel_More);
                    cashInMap.put ("checkResult", Constants.check_Bill_Cash_Out_Channel_More);
                    checkNotOkNum++;
                }
                accessGoldChecks.add (accessGoldCheck);
                channelChecks.add (cashInMap);
            } else {  //系统多出
                checkNotOkNum++;
                cashInMap.put ("checkResult", Constants.check_Bill_Bill_More);
                channelChecks.add (cashInMap);
                //accessGoldCheck.setCheckResult (Constants.check_Bill_Bill_More); //账单系统多出
              //  accessGoldChecks.add (accessGoldCheck);

            }
        }
        try {
//            if (null != accessGoldChecks && accessGoldChecks.size () > 0) {
//                accessGoldCheckDao.updateBatch (accessGoldChecks);//批量更新对账记录
//                if (null != channelChecks && channelChecks.size () > 0) {
//                  accessGoldCheckDao.updateChannelCheckBatch (channelChecks);
//                }
//            }
            if(null!=channelCashList &&channelChecks.size ()>0){
                accessGoldCheckDao.updateChannelCheckBatch (channelChecks);
            }
        } catch (Exception e) {
            log.error ("对账操作处理失败{}", e);
            resultMap.put ("checkStat", Boolean.FALSE);
            return resultMap;
        }
        resultMap.put ("checkStat", Boolean.TRUE);
        resultMap.put ("checkOkNum", checkOkNum);
        resultMap.put ("checkNotOkNum", checkNotOkNum);
        return resultMap;
    }



    /**
     * 上传对账单
     *
     * @param file
     * @return
     */
    @Override
    public Result uploadCheckBill(MultipartFile file) {
        try {
            String md5 = FileUtil.fileMd5 (file.getInputStream ());
            FileInfo fileInfo = fileInfoDao.getById (md5);
            if (null != fileInfo) {
                return Result.error ("对账单已上传");
            }
            File saveFile = new File (file.getOriginalFilename ());
            FileUtils.copyInputStreamToFile (file.getInputStream (), saveFile);
            Map<Integer, List<String>> dataMap = ExcelUtil.getData (saveFile);
            List<AccessGoldCheck> accessGoldCheckList = new ArrayList<> ();
            Iterator<Map.Entry<Integer, List<String>>> entries = dataMap.entrySet ().iterator ();
            while (entries.hasNext ()) {
                Map.Entry<Integer, List<String>> entry = entries.next ();
                List<String> list = entry.getValue ();
                if (null != list && list.size () > 0) {
                    AccessGoldCheck accessGoldCheck = new AccessGoldCheck ();
                    accessGoldCheck.setPayChannel (list.get (0));
                    accessGoldCheck.setTradeCode (list.get (1));
                    accessGoldCheck.setTradeAmount (BigDecimal.valueOf (Float.valueOf (list.get (3))));
                    accessGoldCheck.setChannelFees (BigDecimal.valueOf (Float.valueOf (list.get (4))));
                    accessGoldCheck.setActualChannelFees (BigDecimal.valueOf (Float.valueOf (list.get (5))));
                    accessGoldCheck.setActualArrival (BigDecimal.valueOf (Float.valueOf (list.get (6))));
                    accessGoldCheck.setPayTime ((list.get (7)));
                    String checkKind = list.get (8);
                    try {
                        accessGoldCheck.setCheckKind (checkKind);
                    } catch (Exception e) {
                        return Result.error ("对账类别不能为空！");
                    }
                    Double kind = Double.valueOf (checkKind);
                    String checkKindNum = String.valueOf (kind.intValue ());
                    if (!(Constants.rCheckKind.equals (checkKindNum) || Constants.cCheckKind.equals (checkKindNum))) {
                        return Result.error ("对账类别不对！");
                    }
                    String serialNum = list.get (2);
                    if (null != accessGoldCheckDao.findBySerialNum (serialNum, checkKind)) {
                        return Result.error ("上传对账单中存在错误系统流水号" + serialNum);
                    }
                    accessGoldCheck.setSerialNum (serialNum);
                    accessGoldCheck.setCreateTime (new Date ());
                    accessGoldCheckList.add (accessGoldCheck);
                }
            }
            accessGoldCheckDao.saveAccessGoldChecks (accessGoldCheckList);//上传通道对账单读取对账单数据
            fileService.save (file);
        } catch (IOException e) {
            throw new GlobalException (e.getMessage ());
        }
        return Result.success (CodeMsg.SUCCESS);
    }

    /**
     * 问题对账单处理
     *
     * @param id
     * @param remarks
     * @param conflictAmount
     * @param imageUrl
     * @param checkResult
     * @param checkKind
     */
    @Override
    public void dealProbCashBill(String id, String remarks, String conflictAmount, String imageUrl, String checkResult, String checkKind) {
        Map params = new HashMap ();
        params.put ("id", id);
        params.put ("checkResult", checkResult);
        params.put ("remarks", remarks);
        params.put ("conflictAmount", conflictAmount);//问题金额
        if (Constants.rCheckKind.equals (checkKind)) {
            accessGoldCheckDao.updateCashIn (params);
        } else {
            accessGoldCheckDao.updateCashOut (params);
        }
        if (!StringUtils.isEmpty (imageUrl)) {
            AccessGoldCheck accessGoldCheck = accessGoldCheckDao.findBySerialNum (id, checkKind);
            if (null == accessGoldCheck) {
                throw new GlobalException ("账单不存在");
            }
            accessGoldCheck.setImageUrl (imageUrl);
            accessGoldCheckDao.update (accessGoldCheck);
        }
    }


}

