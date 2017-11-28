package cwm.wearablesdk;

/**
 * Created by user on 2017/11/28.
 */

public class Data {

        private int type;
        private int length;
        private int idType; // to differentiate between ack & nack and message id
        private int messageID;
        private byte[] value;
        private int tag;

        public Data(int type, int length, int idType, int messageID, byte[] value) {
            this.type = type;
            this.length = length;
            this.idType = idType;
            this.messageID = messageID;
            this.value = value;
            this.tag = 0;
        }
        public Data(int type, int length, int idType, int messageID, int tag, byte[] value) {
            this.type = type;
            this.length = length;
            this.idType = idType;
            this.messageID = messageID;
            this.tag = tag;
            this.value = value;
        }
        public int getDataType(){
            return type;
        }

        public int getLength(){return length;}
        public void setLength(int length){
            this.length = length;
        }

        public int getIdType(){
            return idType;
        }

        public int getMessageID(){
            if(idType == ID.ACK ||  idType == ID.NACK)
                return messageID;
            else
                return idType;
        }

        public int getTag(){return tag;}
        public byte[] getValue(){return value;}

}
