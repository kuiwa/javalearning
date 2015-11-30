package cc.openhome;


public class RPG {
    public static void showBlood(Role role) {
        System.out.printf("%s blood %d%n", role.getName(), role.getBlood());
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SwordMan swordman = new SwordMan();
        swordman.setName("Justin");
        swordman.setBlood(100);
        swordman.setLevel(10);
        System.out.printf("SwordMan: (%s, %d, %d)%n", swordman.getName(), swordman.getLevel(), swordman.getBlood());
        Magician magician = new Magician();
        magician.setName("Monica");
        magician.setLevel(20);
        magician.setBlood(60);
        System.out.printf("Magician: (%s, %d, %d)%n", magician.getName(), magician.getLevel(), magician.getBlood());
        showBlood(swordman);
        showBlood(magician);
    }

}
