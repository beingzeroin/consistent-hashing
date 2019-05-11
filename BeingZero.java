import java.util.*;
import java.util.zip.*;

class HashUtils{
    static Long getCRC32Hash(String key){
        CRC32 crc = new CRC32();
        crc.update(key.getBytes());
        return crc.getValue();
    }
}

class Server implements Comparable<Server>{
    private String ipAddress;

    public Server(String ipAddress){
        this.ipAddress = ipAddress;
    }
    public String getIPAddress(){
        return this.ipAddress;
    }

    public String toString(){
        return getIPAddress();
    }
    
    @Override
    public int compareTo(Server o)
    {
        return this.getIPAddress().compareTo(o.getIPAddress());
    }
}

class ConsistentHashing{
    private SortedMap<Long, Server> hashRing;
    private Set<Server> serverSet;

    private int numberOfVirtualNodes;

    public ConsistentHashing(){
        this(5);
    }

    public ConsistentHashing(int virtualNodes){
       this(virtualNodes, null);
    }
    
    public ConsistentHashing(int virtualNodes, Collection<Server> servers){
        this.numberOfVirtualNodes = virtualNodes;
        hashRing = new TreeMap<>();
        serverSet = new TreeSet<>();
        if(servers!=null)
        {
            for(Server s : servers)
                this.addServer(s);
        }
    }

    public void addServer(String serverIP){
        addServer(new Server(serverIP));
    }

    public void addServer(Server server){
        for(int i=0;i<numberOfVirtualNodes;i++){
            String serverVirtualNode = server.getIPAddress() + i;
            hashRing.put(HashUtils.getCRC32Hash(serverVirtualNode), server);
        }
        serverSet.add(server);
    }

    public void removeServer(Server server){
        for(int i=0;i<numberOfVirtualNodes;i++){
            String serverVirtualNode = server.getIPAddress() + i;
            hashRing.remove(HashUtils.getCRC32Hash(serverVirtualNode));
        }
        serverSet.remove(server);
    }

    public Server get(String key){
        if(hashRing.isEmpty())
            return null;
        Long hashedKey = HashUtils.getCRC32Hash(key);
        if(!hashRing.containsKey(hashedKey)){
            SortedMap<Long, Server> tailMap = hashRing.tailMap(hashedKey);
            hashedKey = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        }
        return hashRing.get(hashedKey);
    }

    public void listServers(){
        if(serverSet!=null){
            int i = 1;
            for(Server s : serverSet)
                System.out.printf("\tServer %d: %s\n", i++, s);
            System.out.println();
        }
    }
}

public class BeingZero{
    static Scanner sc = new Scanner(System.in);
    static final int EXIT = 5;
    static final int VNODES_COUNT = 100;
    static Set<String> keySet = new HashSet<>();
    static ConsistentHashing ch;

    static void printKeyMapping(){
        for(String k : keySet){
            System.out.printf("\n\tKey '%s', Server '%s'", k, ch.get(k));
        }
    }
    static int showMenu(){
        System.out.printf("\n\n\tMENU\n");
        System.out.printf("\t==========\n\n");
        System.out.printf("\t1.  Add Server\n");
        System.out.printf("\t2.  Remove Server\n");
        System.out.printf("\t3.  List Servers\n");
        System.out.printf("\t4.  Get Server for Key\n");
        System.out.printf("\t5.  Quit\n\n");
        System.out.printf("\tEnter Your Choice: ");
        return Integer.parseInt(sc.nextLine());
    }
    static Server getServer(){
        System.out.printf("\n\tEnter IP Address of Server: ");
        String serverIP = sc.nextLine();
        return new Server(serverIP);
    }
    static String getKey(){
        System.out.printf("\n\tEnter Key to be Located: ");
        return sc.nextLine().trim();
    }

    static void seedData(){
        String serverIPS[]  = {"198.168.1.1", "198.168.1.2", "198.168.1.3", "198.168.1.4"};
        String keys[]  = {"a", "b", "c", "d", "e", "f", "g" ,"h", "i", "j"};
        for(String ip : serverIPS)
            ch.addServer(ip);
        for(String k : keys)
            keySet.add(k);
        printKeyMapping();
    }
    public static void main(String args[]){
        int choice;
        Server server;
        String key;

        ch = new ConsistentHashing(VNODES_COUNT);

        seedData();
        
        while((choice = showMenu()) !=EXIT){
            switch(choice){
                case 1:
                    server  = getServer();
                    ch.addServer(server);
                    System.out.printf("\n\tServer '%s' Added Successfully\n", server);
                    break;
                case 2:
                    server  = getServer();
                    ch.removeServer(server);
                    System.out.printf("\n\tServer '%s' Removed Successfully\n", server);
                    break;
                case 3:
                    ch.listServers();
                    break;
                case 4:
                    key = getKey();
                    keySet.add(key);
                    server = ch.get(key);
                    System.out.printf("\n\tKey '%s' located on Server '%s'\n", key, server);
                    break;
                case 5:
                    System.out.printf("\n\tThanks for using Consistent Hashing\n");
                    System.exit(0);
                default:
                    System.out.println("\n\tWrong Choice!! Try Again!!");
            }
            printKeyMapping();
        }
    }
}