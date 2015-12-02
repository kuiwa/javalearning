package cc.openhome;


public class RPG {
    public static void showBlood(Role role) {
        System.out.printf("%s blood %d%n", role.getName(), role.getBlood());
    }
    public static void drawFight(Role role) {
        System.out.print(role.getName());
        role.fight();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SwordMan swordman = new SwordMan();
        swordman.setName("Justin");
        swordman.setBlood(100);
        swordman.setLevel(10);
        System.out.printf(swordman.toString());
        Magician magician = new Magician();
        magician.setName("Monica");
        magician.setLevel(20);
        magician.setBlood(60);
        System.out.printf(swordman.toString());
        showBlood(swordman);
        showBlood(magician);
        
        drawFight(swordman);
        drawFight(magician);
    }

}
