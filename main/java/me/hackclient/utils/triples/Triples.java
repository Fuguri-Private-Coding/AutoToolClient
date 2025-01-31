package me.hackclient.utils.triples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Triples<A, B, C> {
    A first;
    B second;
    C third;
}
