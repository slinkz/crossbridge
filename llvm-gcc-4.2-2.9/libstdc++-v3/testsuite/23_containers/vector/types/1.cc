// 2005-12-01  Paolo Carlini  <pcarlini@suse.de>

// Copyright (C) 2005 Free Software Foundation, Inc.
//
// This file is part of the GNU ISO C++ Library.  This library is free
// software; you can redistribute it and/or modify it under the
// terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2, or (at your option)
// any later version.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License along
// with this library; see the file COPYING.  If not, write to the Free
// Software Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
// USA.

// { dg-do compile }

#include <vector>

namespace N
{
  struct X { };

  template<typename T>
    X operator+(T, std::size_t)
    { return X(); }

  template<typename T>
    X operator-(T, T)
    { return X(); }
}

int main()
{
  std::vector<N::X> v(5);
  const std::vector<N::X> w(1);

  v[0];
  w[0];
  v.size();
  v.capacity();
  v.resize(1);
  v.insert(v.begin(), N::X());
  v.insert(v.begin(), 1, N::X());
  v.insert(v.begin(), w.begin(), w.end());
  v = w;

  return 0;
}
