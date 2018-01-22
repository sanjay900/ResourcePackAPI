package net.tangentmc.resourcepackapi.model.predicates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DamagePredicate implements Predicate {
    int damaged;
    double damage;
}
