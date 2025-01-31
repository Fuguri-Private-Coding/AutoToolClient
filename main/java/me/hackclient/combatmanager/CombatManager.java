package me.hackclient.combatmanager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLivingBase;

@Getter @Setter
public class CombatManager {
    double reach = 3;
    EntityLivingBase target;
}
