# Implementations of algorithms found in joint paper by Eder and Perry
# Copyright (C) 2010-2011, the University of Southern Mississippi
# released into the public domain
#
# Originally from http://www.math.usm.edu/perry/Research/basic_sigbased_gb.py
# slightly changed for JAS compatibility, changes are labled with JAS
# $Id: basic_sigbased_gb.py 3804 2011-10-16 21:57:47Z kredel $

# this implementation has one significant difference from the paper:
# the paper maintains monic signatures, but
# this implementation maintains monic polynomials

class sigbased_gb:
  # the base class from which all other classes are derived
  
  def basis_sig(self,F):
    # incremental basis computation
    # F is a container of generators of an ideal
    F.sort(key=lambda x: -x.lm().degree()) # JAS
    #print "JAS F = " + str([ str(g) for g in F]); 
    G = list()
    for f in F:
      G = self.incremental_basis(G,f)
      Gnew = [g[1] for g in G]
      R = f.parent()
      print "size before reduction", len(G)
      G = R.ideal(Gnew).interreduced_basis()
    return G
  
  def spoly_multipliers(self,f,g):
    # multipliers for the s-polynomial of f and g
    # returns uf,ug such that
    # that is, spoly(f,g) = uf.f - ug.g
    tf = f.lm(); tg = g.lm()
    tfg = tf.lcm(tg)
    R = self.R
    return (R.monomial_quotient(tfg,tf),R.monomial_quotient(tfg,tg))
  
  def subset(self,S,criterion):
    # this should be changed to use Python's filter() command
    result = set()
    for s in S:
      if criterion(s):
        result.add(s)
    return result
  
  def min_sig_degree(self,P):
    # determines the minimal degree of a signature in P
    return min([p[0].degree() for p in P])
  
  def new_pair(self,sig,p,q,G):
    # creates a new critical pair from p and q, with signature sig
    # it needs G for the sake of F5; see derived class below
    return (sig,p,q)
  
  def spoly(self,s,G):
    # computes the spolynomial
    # assumes that s has the form (signature, poly, poly)
    f = s[1]; g = s[2]
    tf = f.lm(); tg = g.lm()
    tfg = tf.lcm(tg)
    R = f.parent()
    uf = R.monomial_quotient(tfg,tf); ug = R.monomial_quotient(tfg,tg)
    return uf*f - ug*g
  
  def initialize_Syz(self,F,G):
    # initializes Syz; initially, this does nothing
    return set()
  
  def prune_P(self,P,Syz):
    # prunes P using Syz; initially, this does nothing
    return P
    
  def prune_S(self,S,Syz,Done,G):
    # prunes S using Syz, Done, and G; initially, this does nothing
    # (Done is used as a shortcut)
    return S
  
  def update_Syz(self,Syz,sigma,r):
    # updates Syz using sigma and r
    # some algorithms use this; some don't
    return Syz
  
  def sigsafe_reduction(self,s,sigma,G,F,Syz):
    # computes a complete sigma-reduction of s modulo G
    # F is assumed to be a subset of G that represents the previous GB incrementally
    # Syz is sent but not used (I should probably remove this)
    r = s
    r_sigma = sigma
    R = self.R
    reduced = True
    while (r != 0) and reduced:
      reduced = False
      r = r.reduce(F)
      if any(g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm()) for g in G):
        for g in G:
          if g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm()):
            u = self.R.monomial_quotient(r.lt(),g[1].lt(),coeff=True)
            sig_ug = u*g[0]
            if (sig_ug < r_sigma) or ((sig_ug.lm() == r_sigma.lm()) and (sig_ug.lc() != r_sigma.lc())):
              reduced = True
              r -= u*g[1]
              if (sig_ug.lm() == r_sigma.lm()):
                r_sigma -= sig_ug
    # ensure that r is monic
    if r != 0:
      c = r.lc()
      r *= c**(-1)
      r_sigma *= c**(-1)
    return r_sigma, r
    
  def sig_redundant(self,sigma,r,G):
    # test whether (sigma,r) is signature-redundant wrt G
    R = self.R
    return any(g[0] != 0 and R.monomial_divides(g[0].lm(),sigma.lm()) and R.monomial_quotient(sigma.lm(),g[0].lm())*g[1].lm()==r.lm() for g in G)
    #if any ((g[0] != 0 and R.monomial_divides(g[0].lm(),sigma.lm())) and (g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm())) and not R.monomial_quotient(sigma.lm(),g[0].lm())*g[1].lm()==r.lm() for g in G):
    #  print "counterexample at", (sigma, r.lm())
    return any ((g[0] != 0 and R.monomial_divides(g[0].lm(),sigma.lm())) and (g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm())) for g in G)
  
  def incremental_basis(self,F,g):
    # assuming that F is a Groebner basis of the ideal generated by F,
    # compute a Groebner basis of F+[g]
    self.R = g.parent(); R = self.R
    # to record a signature, we use only the leading monomial of a minimal representation
    # so that elements of F have "signature" 0 and g has "signature" 1
    G = [(R(0),F[i]) for i in xrange(len(F))] + [(R(1),g)]
    #print "JAS G = " + str([ str(gg[0])+","+str(gg[1]) for gg in G]); 
    # the structure of a pair can vary, except for its first entry,
    # which should be the signature
    P = set([self.new_pair(self.spoly_multipliers(g,f)[0],g,f,G) for f in F])
    Syz = self.initialize_Syz(F,G)
    # Done will track new polynomials computed by the algorithm
    Done = list()
    while len(P) != 0:
      P = self.prune_P(P,Syz)
      if len(P) != 0:
        S = list(self.subset(P,lambda x: x[0].degree() == self.min_sig_degree(P)))
        print "treating", len(S), "signatures of degree", self.min_sig_degree(P)
        P.difference_update(S)
        while len(S) != 0:
          S = self.prune_S(S,Syz,Done,G)
          if len(S) != 0:
            # sort by signature
            S.sort(key=lambda x:x[0]); s = S.pop(0)
            sigma,r = self.sigsafe_reduction(self.spoly(s,G),s[0],G,F,Syz)
            if (r != 0) and (not self.sig_redundant(sigma,r,G)): 
              #print "new polynomial", (sigma,r.lm())
              for (tau,g) in G:
                if (g != 0):
                  rmul,gmul = self.spoly_multipliers(r,g)
                  if rmul*sigma.lm() != gmul*tau.lm():
                    if rmul*sigma.lm() > gmul*tau.lm():
                      p = self.new_pair(rmul*sigma,r,g,G)
                    else:
                      p = self.new_pair(gmul*tau,g,r,G)
                    if p[0].degree() == sigma.degree():
                      S.append(p)
                    else:
                      P.add(p)
              G.append((sigma,r))
              Done.append((sigma,r))
            elif r == 0:
              #print "zero reduction at", (sigma,r.lm()) 
              self.update_Syz(Syz,sigma,r)
              Done.append((sigma,r))
            #else:
              #print "sig-redundant at", sigma
    return list(self.subset(G,lambda x: x[1] != 0))

class ggv(sigbased_gb):
  # the plugin implementation of ggv
  
  def new_pair(self,sig,p,q,G):
    # creates a new critical pair from p and q, with signature sig
    # it needs G for the sake of F5; see derived class below
    i = -1; j = -1; k = 0
    up,uq = self.spoly_multipliers(p,q)
    while (i<0 or j<0) and k < len(G):
      if p == G[k][1]:
        i = k
      elif q == G[k][1]:
        j = k
      k += 1;
    if (i == -1):
      i=len(G)
    elif (j == -1):
      j = len(G)
    return (sig,i,j)
  
  def initialize_Syz(self,F,G):
    # recognize trivial syzygies
    return set([f.lm() for f in F])
    
  def spoly(self,s,G):
    # ggv only computes part of an S-polynomial
    # (as if it were computing a row of the Macaulay matrix
    # and not subsequently triangularizing)
    f = G[s[1]][1]; g = G[s[2]][1]
    tf = f.lm(); tg = g.lm()
    tfg = tf.lcm(tg)
    uf = self.R.monomial_quotient(tfg,tf)
    return uf*f
  
  def prune_P(self,P,Syz):
    # remove any pair whose signature is divisible by an element of Syz
    result = set()
    R = self.R
    for p in P:
      if not any(R.monomial_divides(t,p[0]) for t in Syz):
        result.add(p)
    return result
  
  def prune_S(self,S,Syz,Done,G):
    # watch out for new syzygies discovered, and allow only one polynomial
    # per signature
    result = list()
    R = self.R
    for s in S:
      if not any(R.monomial_divides(t,s[0]) for t in Syz):
        if not any(s[0].lm()==sig[0].lm() and s[1]<sig[1] for sig in S):
          if not any(s[0].lm()==sig[0].lm() for sig in result):
            result.append(s)
    return result
  
  def update_Syz(self,Syz,sigma,r):
    # add non-trivial syzygies to the basis
    # polynomials that reduce to zero indicate non-trivial syzygies
    if r == 0:
      Syz.add(sigma.lm())
    return Syz

class ggv_first_implementation(ggv):
  
  def new_pair(self,sig,p,q,G):
    # creates a new critical pair from p and q, with signature sig
    # it needs G for the sake of F5; see derived class below
    i = -1; j = -1; k = 0
    return (sig,p,q)
  
  def spoly(self,s,G):
    # ggv only computes part of an S-polynomial
    # (as if it were computing a row of the Macaulay matrix
    # and not subsequently triangularizing)
    # -- at least, that's how I read "(t_i,m+1)" on p. 6 of that paper
    f = s[1]; g = s[2]
    tf = f.lm(); tg = g.lm()
    tfg = tf.lcm(tg)
    uf = self.R.monomial_quotient(tfg,tf)
    return uf*f

  def prune_S(self,S,Syz,Done,G):
    # watch out for new syzygies discovered, and allow only one polynomial
    # per signature
    result = list()
    R = self.R
    for s in S:
      if not any(R.monomial_divides(t,s[0]) for t in Syz):
        if not any(s[0].lm()==sig[0].lm() for sig in Done):
          result.append(s)
    return result
  
class coeff_free_sigbased_gb(sigbased_gb):
  # child class of sigbased_gb that implements semi-complete reduction
  
  def sigsafe_reduction(self,s,sigma,G,F,Syz):
    # see sigbased_gb.sigsafe_reduction
    r = s
    r_sigma = sigma
    R = self.R
    reduced = True
    while (r != 0) and reduced:
      reduced = False
      r = r.reduce(F)
      if any( g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm()) for g in G ):
        for g in G:
          if g[1] != 0 and R.monomial_divides(g[1].lm(),r.lm()):
            u = self.R.monomial_quotient(r.lt(),g[1].lt(),coeff=True)
            sig_ug = u*g[0]
            if sig_ug.lm() < r_sigma:
              reduced = True
              r -= u*g[1]
    # ensure that r is monic
    if r != 0:
      c = r.lc()
      r *= c**(-1)
    return r_sigma, r
    
class arris_algorithm(coeff_free_sigbased_gb):
  # the plugin implementation of arri's algorithm
  
  def initialize_Syz(self,F,G):
    # recognize trivial syzygies
    return set([f.lm() for f in F])
    
  def update_Syz(self,Syz,sigma,r):
    # add non-trivial syzygies to the basis
    # polynomials that reduce to zero indicate non-trivial syzygies
    if r == 0:
      Syz.add(sigma.lm())
    return Syz
  
  def prune_P(self,P,Syz):
    # remove any pair whose signature is divisible by an element of Syz
    result = set()
    for p in P:
      if not any(self.R.monomial_divides(t,p[0]) for t in Syz):
        result.add(p)
    return result
  
  def prune_S(self,S,Syz,Done,G):
    # watch out for new syzygies discovered, and apply arri's rewritable criterion:
    # for any s-polynomial of a given signature, if there exists another (s-)polynomial
    # in S or Done of identical signature but lower lm, discard the first
    result = list()
    for s in S:
      if not any(self.R.monomial_divides(t,s[0]) for t in Syz):
        if not any(s[0]==sig[0] and s[1].lm()>sig[1].lm() for sig in S):
          for (sig,f) in Done:
            if self.R.monomial_divides(sig,s[0]):
              u = self.R.monomial_quotient(s[0],sig)
              if u*f.lm() < s[1].lm():
                break
          else:
            result.append(s)
    return result
  
  def new_pair(self,sig,p,q,G):
    # in arri's algorithm, each pair is (sigma,s) where s is the s-polynomial
    # and sigma is its natural signature
    tp = p.lm(); tq = q.lm()
    tpq = tp.lcm(tq)
    R = p.parent() #JAS tpq.parent()
    up = R.monomial_quotient(tpq,tp); uq = R.monomial_quotient(tpq,tq)
    return (sig,up*p-uq*q)
  
  def spoly(self,s,G):
    return s[1]

class f5(coeff_free_sigbased_gb):
  # the plugin implementation of arri's algorithm
  
  def initialize_Syz(self,F,G):
    # recognize trivial syzygies
    return set([f.lm() for f in F])
    
  def update_Syz(self,Syz,sigma,r):
    # recognize trivial syzygies
    # see class f5z for a more thorough update_Syz in line w/arri and ggv
    return Syz
  
  def prune_P(self,P,Syz):
    # remove any pair whose signature is divisible by an element of Syz
    result = set()
    for p in P:
      if not any(self.R.monomial_divides(t,p[0]) for t in Syz):
        result.add(p)
    return result
  
  def prune_S(self,S,Syz,Done,G):
    # watch out for new syzygies discovered, and apply faugere's rewritable criterion:
    # for any (sigma,p,q) in S, if there exists (tau,g) such that tau divides sigma
    # but g was generated after p, discard (sigma,p,q)
    result = list()
    for (sig,u,j,v,k) in S:
      if not any(self.R.monomial_divides(t,sig) for t in Syz):
        if G[j][0] == 0 or not any(self.R.monomial_divides(Done[i][0],G[j][0]*u) and Done[i][0] > G[j][0] for i in xrange(len(Done))):
          result.append((sig,u,j,v,k))
    return result
  
  def new_pair(self,sig,p,q,G):
    # it's easier to deal with faugere's criterion if one creates pairs
    # using indices rather than polynomials
    # note that this while look gives f5 a disadvantage
    i = -1; j = -1; k = 0
    up,uq = self.spoly_multipliers(p,q)
    while (i<0 or j<0) and k < len(G):
      if p == G[k][1]:
        i = k
      elif q == G[k][1]:
        j = k
      k += 1;
    if (i == -1):
      i=len(G)
    elif (j == -1):
      j = len(G)
    return (sig,up,i,uq,j)
    
  def spoly(self,s,G):
    # since s has the structure (sigma,up,i,uq,j)
    # we have to compute the s-polynomial by looking up f and g
    f = G[s[2]][1]; g = G[s[4]][1]
    uf = s[1]; ug = s[3]
    return uf*f - ug*g
    
class f5z(f5):
  
  def update_Syz(self,Syz,sigma,r):
    # recognize trivial syzygies
    if r == 0:
      Syz.add(sigma.lm())
    return Syz
    
class min_size_mons(arris_algorithm):
  # the plugin implementation of arri's algorithm
  
  def prune_S(self,S,Syz,Done,G):
    # watch out for new syzygies discovered, and apply the minimal "number of monomials" criterion:
    # for any s-polynomial of a given signature, if there exists another polynomial
    # in Done of identical signature but fewer monomials, replace this s-polynomial
    # by the multiple of the polynomial with fewer monomials
    result = list()
    R = G[0][1].parent()
    for (sigma,s) in S:
      if not any(R.monomial_divides(tau,sigma) for tau in Syz):
        if not any(tau == sigma for (tau,g) in result):
          for (tau,g) in Done:
            if tau.divides(sigma) and len(g.monomials()) < len(s.monomials()):
              u = R.monomial_quotient(sigma,tau)
              result.append((u*tau,u*g))
              break
          else:
            result.append((sigma,s))
    return result
    
