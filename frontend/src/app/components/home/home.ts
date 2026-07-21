import { Component } from '@angular/core';
import { HomeHero } from './hero/home-hero';
import { HomeContas } from './contas/home-contas';
import { HomeCartoes } from './cartoes/home-cartoes';
import { BottomNav } from './bottom-nav/bottom-nav';
import { LayoutBanco } from '../shared/layout-banco/layout-banco';

@Component({
  selector: 'app-home',
  imports: [HomeHero, HomeContas, HomeCartoes, BottomNav, LayoutBanco],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {}
