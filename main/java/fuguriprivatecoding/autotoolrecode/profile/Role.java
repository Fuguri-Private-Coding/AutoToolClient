package fuguriprivatecoding.autotoolrecode.profile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // луф когда новую роль добавлять будешь НЕ забудь пофиксить левелы
    // потому что если не сделать этого то все взорваться может
    NONE(0, "null", "тут сам поставь типо везде знак параграфа и цвет через кодовое значение и потом в рендере сам укажи ок да"),
    USER(1, "User", ""),
    TESTER(2, "Tester", ""),
    MODERATOR(3, "Moderator", ""),
    OWNER(4, "Owner", "");

    private final int level;
    private final String name;
    private final String colorPrefix;

    /**
     * Если у {@code other} такой-же уровень доступа, то вернет {@code true} иначе {@code false}
     * @param other роль для сравнения доступа
     * @return Равны ли уровни доступа
     */
    public boolean isEquals(Role other) {
        return level == other.level;
    }

    /**
     * Если у {@code other} уровень доступа ниже, то вернет {@code true} иначе {@code false}
     * @param other роль для сравнения доступа
     * @return Ниже ли уровень доступа
     */
    public boolean isHigherThen(Role other) {
        return level > other.level;
    }

    /**
     * Если у {@code other} уровень доступа ниже или он равен, то вернет {@code true} иначе {@code false}
     * @param other роль для сравнения доступа
     */
    public boolean isHigherThenOrEquals(Role other) {
        return isHigherThen(other) || isEquals(other);
    }

    /**
     * Если у {@code other} уровень доступа выше, то вернет {@code true} иначе {@code false}
     * @param other роль для сравнения доступа
     * @return Выше ли уровень доступа
     */
    public boolean isLowerThen(Role other) {
        return level < other.level;
    }

    /**
     * Если у {@code other} уровень доступа выше или он равен, то вернет {@code true} иначе {@code false}
     * @param other роль для сравнения доступа
     */
    public boolean isLowerThenOrEquals(Role other) {
        return isLowerThen(other) || isEquals(other);
    }

    /**
     * Получение объекта роли по его названию без учета регистра
     * @param name Название роли
     * @return Объект роли
     */
    public static Role fromRoleName(String name) {
        for (Role value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return NONE;
    }
}
