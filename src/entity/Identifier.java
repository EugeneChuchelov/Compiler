package entity;

public class Identifier {
    private int id;

    private String type;

    public Identifier(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Identifier){
            return id == ((Identifier)obj).id;
        } else {
            return false;
        }
    }
}
