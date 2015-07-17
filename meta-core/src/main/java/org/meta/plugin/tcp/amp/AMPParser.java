package org.meta.plugin.tcp.amp;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import org.meta.api.model.ModelFactory;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Parse an AMP message 
 * @author faquin
 */
public abstract class AMPParser {
    private Logger logger = LoggerFactory.getLogger(AMPParser.class);
    protected ModelFactory factory = null;
    
    public AMPParser(byte[] bs, ModelFactory factory) throws InvalidAMPCommand{
        this.factory = factory;
        parse(bs);
    }
    public AMPParser(byte[] bs) throws InvalidAMPCommand{
        parse(bs);
    }

    /**
     * parse an byte[] as describe in the AMP Protocol
     * Once the hashMap containing keyMap is rebuild, it call useContent
     * wich has to be override in an extending class
     * 
     * @param bs the byte array to parse
     */
    private void parse(byte[] bs) throws InvalidAMPCommand{
        LinkedHashMap<String, byte[]> content = new LinkedHashMap<String, byte[]>();
        int readIndex = 0;

        if(bs.length > 0){
            //for each elements in the byte array
            while(bs[readIndex] != 0x00 || bs[readIndex+1] != 0x00){
                //recompose the size of the following bloc
                int     size     = parseSize(bs[readIndex], bs[readIndex+1]);

                String     name    = null;
                byte[]     value    = null;

                //increase offset
                readIndex += 2;

                //user a stringBuilder as a buffer to rebuild the name value
                StringBuilder builder = new StringBuilder();
                for(int i=readIndex; (i-readIndex)<size; i++){
                    builder.append((char)bs[i]);
                }
                //increase the offset by the readed size
                readIndex = readIndex + size;
                name = builder.toString();

                //size of the value
                size = parseSize(bs[readIndex], bs[readIndex+1]);
                readIndex += 2;

                //Stack the value directly in a byte[]
                value = new byte[size];
                for(int i=readIndex; (i-readIndex)<size; i++){
                    value[i-readIndex] = bs[i];
                }
                //increase the offset
                readIndex = readIndex + size;
                content.put(name, value);
            }
        }else{
            throw new InvalidAMPCommand("content cannot be null");
        }
        useContent(content);
    }

    /**
     * use this content to rebuild what you want
     * @param content an LinkedhashMap containing key value
     * where key si the name of what the value represent
     * 
     * @throws InvalidAMPCommand
     */
    protected abstract void useContent(LinkedHashMap<String, byte[]> content) throws InvalidAMPCommand;

    /**
     * rebuild a short from a byte array
     * @param bytes byte array containing two index
     * @return a short
     */
    private int parseSize(byte ... bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

}