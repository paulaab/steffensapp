package de.vodafone.innogarage.sfcdi_monitor;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScannerTask extends AsyncTask<ConnectionManager, Void, List<String>> {



    @Override
    protected List<String> doInBackground(ConnectionManager... params) {

        ConnectionManager conMan = null;
        if(params.length == 1)
            conMan = params[0];

        Connection con = null;
        List<JSONObject> msgList = null;
        JSONObject actMsg = null;
        CopyOnWriteArrayList<String> rs = new CopyOnWriteArrayList<>();
        List<Connection> cons = null;

        if(!conMan.getConnections().isEmpty()){


            cons = conMan.getConnections();
            for(Connection temp : cons){

                if(temp.isFocus()){
                    con = temp;
                }
            }

            if(con == null){

                con = cons.get(0);
            }

            msgList = con.getIncomingData();

            if(!msgList.isEmpty()) {


                actMsg = msgList.get(0);
                String msgType = null;
                try {
                    msgType = actMsg.getString("msgType");
                    msgType = removeJSONShit(msgType);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(msgType.equalsIgnoreCase("gstatus")) {

                    msgList.remove(0);

                    String  name = null,
                            temperature = null,
                            currenttime = null,
                            resetcounter = null,
                            mode = null,
                            systemmode = null,
                            psstate = null,
                            lteband = null,
                            ltebw = null,
                            imsregstate = null,
                            lterxchan  = null,
                            ltetxchan = null,
                            ltecastate = null,
                            emmstate = null,
                            rrcstate = null,
                            rxmrssi = null,
                            rxmrsrp = null,
                            txpower = null,
                            rxdrssi = null,
                            rxdrsrp = null,
                            tac = null,
                            rsrq = null,
                            sinr = null,
                            cellid = null;

                    try {
                        name = con.getName();
                        temperature =   removeJSONShit(actMsg.getString("Temperature"));
                        currenttime =   removeJSONShit(actMsg.getString("Current Time"));
                        resetcounter =  removeJSONShit(actMsg.getString("Reset Counter"));
                        mode =          removeJSONShit(actMsg.getString("Mode"));
                        systemmode =    removeJSONShit(actMsg.getString("System mode"));
                        psstate =       removeJSONShit(actMsg.getString("PS state"));
                        lteband =       removeJSONShit(actMsg.getString("LTE band"));
                        ltebw =         removeJSONShit(actMsg.getString("LTE bw"));
                        imsregstate =   removeJSONShit(actMsg.getString("IMS reg state"));
                        lterxchan  =    removeJSONShit(actMsg.getString("LTE Rx chan"));
                        ltetxchan =     removeJSONShit(actMsg.getString("LTE Tx chan"));
                        ltecastate =    removeJSONShit(actMsg.getString("LTE CA state"));
                        emmstate =      removeJSONShit(actMsg.getString("EMM state"));
                        rrcstate =      removeJSONShit(actMsg.getString("RRC state"));
                        rxmrssi =       removeJSONShit(actMsg.getString("PCC RxM RSSI"));
                        txpower =       removeJSONShit(actMsg.getString("Tx Power"));
                        rxdrssi =       removeJSONShit(actMsg.getString("PCC RxD RSSI"));
                        tac =           removeJSONShit(actMsg.getString("TAC"));
                        rsrq =          removeJSONShit(actMsg.getString("RSRQ (dB)"));
                        sinr =          removeJSONShit(actMsg.getString("SINR (dB)"));
                        cellid =        removeJSONShit(actMsg.getString("Cell ID"));

                        String[] tempX =  removeJSONShit(actMsg.getString("RSRP (dBm)")).split(",");

                        rxmrsrp = tempX[0].substring(0,tempX[0].length()-1) ;
                        rxdrsrp = tempX[1].substring(1,tempX[1].length()) ;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(name != null) rs.add(name);
                    else rs.add("UNKNOWN");

                    if(temperature != null) rs.add(temperature);
                    else rs.add("N/A!");

                    if(currenttime != null) rs.add(currenttime);
                    else rs.add("N/A!");

                    if(resetcounter != null) rs.add(resetcounter);
                    else rs.add("N/A!");


                    if(mode != null){
                        if(mode.equalsIgnoreCase("online")){
                            con.setOnline(true);
                        }
                        else{
                            con.setOnline(false);
                        }
                        rs.add(mode);
                    }
                    else {
                        rs.add("N/A!");
                    }


                    if(systemmode != null) rs.add(systemmode);
                    else rs.add("N/A!");

                    if(psstate != null) rs.add(psstate);
                    else rs.add("N/A!");

                    if(lteband != null) rs.add(lteband);
                    else rs.add("N/A!");

                    if(ltebw != null) rs.add(ltebw);
                    else rs.add("N/A!");

                    if(imsregstate != null) rs.add(imsregstate);
                    else rs.add("N/A!");

                    if(lterxchan != null) rs.add(lterxchan);
                    else rs.add("N/A!");

                    if(ltetxchan != null) rs.add(ltetxchan);
                    else rs.add("N/A!");

                    if(ltecastate != null) rs.add(ltecastate);
                    else rs.add("N/A!");

                    if(emmstate != null) rs.add(emmstate);
                    else rs.add("N/A!");

                    if(rrcstate != null) rs.add(rrcstate);
                    else rs.add("N/A!");

                    if(rxmrssi != null) rs.add(rxmrssi);
                    else rs.add("N/A!");

                    if(txpower != null) rs.add(txpower);
                    else rs.add("N/A!");

                    if(rxdrssi != null) rs.add(rxdrssi);
                    else rs.add("N/A!");

                    if(tac != null) rs.add(tac);
                    else rs.add("N/A!");

                    if(rsrq != null) rs.add(rsrq);
                    else rs.add("N/A!");

                    if(sinr != null) rs.add(sinr);
                    else rs.add("N/A!");

                    if(cellid != null) rs.add(cellid);
                    else rs.add("N/A!");

                    if(rxmrsrp != null) rs.add(rxmrsrp);
                    else rs.add("N/A!");

                    if(rxdrsrp != null) rs.add(rxdrsrp);
                    else rs.add("N/A!");
                }
            }
        }
        return rs;
    }

    private String removeJSONShit(String mitShit){

        String ohneShit = "";
        ohneShit = mitShit.substring(2,mitShit.length()-2);
        return ohneShit;
    }
}