package net.tangentmc.resourcepackapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.resourcepackapi.model.predicates.Predicate;

@AllArgsConstructor
@Getter
@Setter
public class Override {
    private Predicate predicate;
    private String model;
}
