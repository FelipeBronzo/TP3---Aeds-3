package model;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Serie {
    private int id; 
    private String name;
    private String language;
    private Date first_air_date;
    private ArrayList<String> companies;
    private SimpleDateFormat formatodedata = new SimpleDateFormat("dd/MM/yyyy");

    public Serie(){
        this.id = -1;
        this.name = "unnamed";
        this.language = "xx";
        this.first_air_date = null;
        this.companies = new ArrayList<String>();
    }

    public Serie(int id, String name, String language, Date first_air_date, ArrayList<String> companies){
        this.id = id;
        this.name = name;
        this.language = language;
        this.first_air_date = first_air_date;
        this.companies = companies;
    }

    //----- get and set (id) -----
    public int getId(){
        return id;
    } 
    public void setId(int id){
        this.id = id;
    }

    //----- get and set (name) -----
    public String getName(){
        return name;
    } 
    public void setName(String name){
        this.name = name;
    }

    //----- get and set (language) -----
    public String getLanguage (){
        return language;
    } 
    public void setLanguage(String language){
        this.language = language;
    }

    //----- get and set (date) -----
    public String getDate(){
        return formatodedata.format(first_air_date);
    } 

    public void setDate(String first_air_datee){
        try{
            Date dataformatada = formatodedata.parse(first_air_datee);
            this.first_air_date = dataformatada; 
        } 
        catch(ParseException e) {
            System.out.println("Erro ao converter a data: " + e.getMessage());
        }
    }

    //----- get and set (companies) ----- 
    public ArrayList<String> getCompanies(){
        return companies;
    } 
    public void setCompanies(String companies){
        if (!(companies.equals("-")))
        this.companies.add(companies);
    }

    // le a linha do csv fornecida, separa por ; e seta atributo por atributo desse novo objeto
    public void ler(String line){
        // Splita a linha
        String data[] = line.split(";");
        if(data.length >= 5){
            try{
                int idconverted = Integer.parseInt(data[0]);
                setId(idconverted);
            }
            catch (NumberFormatException e) {
                System.out.println("Error when converting: " + e.getMessage());
            }
        }
        setName(data[1]);
        setLanguage(data[2]);
        setDate(data[3]);
        setCompanies(data[4]);
        setCompanies(data[5]);
        setCompanies(data[6]);
    }

    @Override
    public String toString(){
        long dateInSeconds = first_air_date.getTime();
        return "\nID: " + id + "\nName: " + name + "\nLanguage: " + language + "\nDate: " + dateInSeconds + "\nCompanies: " + companies;
    }

    // Escrita em array de bytes, incluindo conversão de data para segundos
    public byte[] toByteArray() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(name);
        dos.writeUTF(language);
        
        //Converte a data para segundos antes de escrever
        long dateInSeconds = first_air_date.getTime();
        dos.writeLong(dateInSeconds);

        // Escrever o ArrayList de Strings (companias)
        dos.writeInt(companies.size());  // Primeiro, o tamanho da lista
        StringBuilder companiesString = new StringBuilder();
        for (int i = 0; i < companies.size(); i++) {
            companiesString.append(companies.get(i));
            if (i < companies.size() - 1) {
                companiesString.append(",");  // Adiciona vírgula entre os nomes das companhias
            }
        }
        dos.writeUTF(companiesString.toString());

        dos.close();
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
    
        this.id = dis.readInt();
        this.name = dis.readUTF();
        this.language = dis.readUTF();
    
        long dateInSeconds = dis.readLong();
        this.first_air_date = new Date(dateInSeconds);
    
        @SuppressWarnings("unused")
        int numCompanies = dis.readInt();
        
        this.companies = new ArrayList<>();
        String companiesString = dis.readUTF();
        String[] companiesArray = companiesString.split(",");
        for (String company : companiesArray) {
            this.companies.add(company);
        }
    
        dis.close();
    }
}
