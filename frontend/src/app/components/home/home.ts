import { Component } from '@angular/core';
import { HomeHero } from './hero/home-hero';
import { HomeContas } from './contas/home-contas';
import { HomeCartoes } from './cartoes/home-cartoes';
import { BottomNav } from './bottom-nav/bottom-nav';

@Component({
  selector: 'app-home',
  imports: [HomeHero, HomeContas, HomeCartoes, BottomNav],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {}
